/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.portaaltaak

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.notificatiesapi.exception.NotificatiesNotificationEventException
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valtimo.operaton.domain.OperatonTask
import com.ritense.valtimo.service.OperatonProcessService
import com.ritense.valtimo.service.OperatonTaskService
import com.ritense.valueresolver.ValueResolverService
import mu.KotlinLogging
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.delegate.VariableScope
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import java.net.MalformedURLException
import java.net.URI
import java.util.UUID

open class PortaalTaakEventListener(
    private val objectManagementService: ObjectManagementService,
    private val pluginService: PluginService,
    private val processDocumentService: ProcessDocumentService,
    private val processService: OperatonProcessService,
    private val taskService: OperatonTaskService,
    private val runtimeService: RuntimeService,
    private val valueResolverService: ValueResolverService,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    @RunWithoutAuthorization
    @EventListener(NotificatiesApiNotificationReceivedEvent::class)
    open fun processCompletePortaalTaakEvent(event: NotificatiesApiNotificationReceivedEvent) {
        logger.debug { "Received Notificaties API event, checking if it fits criteria to complete a portaaltaak" }

        val objectType = event.kenmerken["objectType"]
        if (!event.kanaal.equals("objecten", ignoreCase = true) ||
            !event.actie.equals("update", ignoreCase = true) ||
            objectType == null
        ) {
            logger.debug { "Notificaties API event does not match criteria for completing a portaaltaak. Ignoring." }
            return
        }

        val objectTypeId = objectType.substringAfterLast("/")

        val objectManagement =
            objectManagementService.findByObjectTypeId(objectTypeId)
                ?: run {
                    logger.warn { "Object management not found for object type id '$objectTypeId'" }
                    return
                }

        pluginService.findPluginConfiguration(PortaaltaakPlugin::class.java) { properties: JsonNode ->
            properties.get("objectManagementConfigurationId").textValue().equals(objectManagement.id.toString())
        }?.let {
            logger.debug { "Completing portaaltaak using plugin configuration with id '${it.id}'" }

            val taakObject: TaakObject = objectMapper.convertValue(getPortaalTaakObjectData(objectManagement, event))
            when (taakObject.status) {
                TaakStatus.INGEDIEND -> {
                    logger.debug { "Processing task with status 'ingediend' and verwerker task id '${taakObject.verwerkerTaakId}'" }
                    val task = taskService.findTaskById(taakObject.verwerkerTaakId)
                        ?: run {
                            logger.warn { "Task not found with verwerker task id '${taakObject.verwerkerTaakId}'" }
                            return
                        }

                    val receiveData = getReceiveDataActionProperty(task, it.id.id) ?: return

                    val portaaltaakPlugin = pluginService.createInstance(it) as PortaaltaakPlugin
                    val processInstanceId = OperatonProcessInstanceId(task.getProcessInstanceId())
                    val documentId = runWithoutAuthorization {
                        processDocumentService.getDocumentId(processInstanceId, task)
                    }
                    saveDataInDocument(taakObject, task, receiveData)
                    startProcessToUploadDocuments(
                        taakObject,
                        portaaltaakPlugin.completeTaakProcess,
                        documentId.id.toString(),
                        objectManagement.objectenApiPluginConfigurationId.toString(),
                        event.resourceUrl
                    )
                }

                else -> {
                    logger.debug { "Taak status is not 'ingediend', skipping completion of portaaltaak" }
                }
            }
        }
            ?: logger.warn { "No portaaltaak plugin configuration found with object management configuration id '${objectManagement.id}'" }
    }

    private fun getReceiveDataActionProperty(task: OperatonTask, pluginConfigurationId: UUID): List<DataBindingConfig>? {
        logger.debug { "Retrieving receive data action property for task with id '${task.id}'" }
        val processLinks = pluginService.getProcessLinks(task.getProcessDefinitionId(), task.taskDefinitionKey!!)
        val processLink = processLinks.firstOrNull { processLink ->
            processLink.pluginConfigurationId == pluginConfigurationId
        }

        val receiveDataJsonNode = processLink?.actionProperties?.get("receiveData") ?: run {
            logger.warn { "No receive data for task with id '${task.id}'" }
            return null
        }

        val typeRef = object : TypeReference<List<DataBindingConfig>>() {}
        return objectMapper.treeToValue(receiveDataJsonNode, objectMapper.constructType(typeRef))
    }

    internal fun saveDataInDocument(
        taakObject: TaakObject,
        task: OperatonTask,
        receiveData: List<DataBindingConfig>
    ) {
        logger.debug { "Saving data in document for task with id '${task.id}'" }
        if (taakObject.verzondenData.isNotEmpty()) {
            val processInstanceId = OperatonProcessInstanceId(task.getProcessInstanceId())
            val variableScope = getVariableScope(task)
            val taakObjectData = objectMapper.valueToTree<JsonNode>(taakObject.verzondenData)
            val resolvedValues = getResolvedValues(receiveData, taakObjectData)
            handleTaakObjectData(processInstanceId, variableScope, resolvedValues)
        } else {
            logger.warn { "No data found in taakobject for task with id '${task.id}'" }
        }
    }

    /**
     * @param receiveData: [ doc:/streetName  to  "/persoonsData/adres/straatnaam" ]
     * @param data {"persoonsData":{"adres":{"straatnaam":"Funenpark"}}}
     * @return mapOf(doc:/streetName to "Funenpark")
     */
    private fun getResolvedValues(receiveData: List<DataBindingConfig>, data: JsonNode): Map<String, Any> {
        return receiveData.associateBy({ it.key }, { getValue(data, it.value) })
    }

    private fun getValue(data: JsonNode, path: String): Any {
        val valueNode = data.at(JsonPointer.valueOf(path))
        if (valueNode.isMissingNode) {
            throw RuntimeException("Failed to find path '$path' in data: \n${data.toPrettyString()}")
        }
        return objectMapper.treeToValue(valueNode, Object::class.java)
    }

    private fun handleTaakObjectData(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        resolvedValues: Map<String, Any>
    ) {
        if (resolvedValues.isNotEmpty()) {
            valueResolverService.handleValues(processInstanceId.toString(), variableScope, resolvedValues)
        }
    }

    private fun getVariableScope(task: OperatonTask): VariableScope {
        return runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.getProcessInstanceId())
            .singleResult() as VariableScope
    }

    internal fun getDocumentenUrls(verzondenData: JsonNode): List<String> {
        val documentPathsNode = verzondenData.at(JsonPointer.valueOf("/documenten"))
        if (documentPathsNode.isMissingNode || documentPathsNode.isNull) {
            return emptyList()
        }
        if (!documentPathsNode.isArray) {
            throw NotificatiesNotificationEventException(
                "Could not retrieve document Urls.'/documenten' is not an array"
            )
        }
        val documentenUris = mutableListOf<String>()
        for (documentPathNode in documentPathsNode) {
            val documentUrlNode = verzondenData.at(JsonPointer.valueOf(documentPathNode.textValue()))
            if (!documentUrlNode.isMissingNode && !documentUrlNode.isNull) {
                try {
                    if (documentUrlNode.isTextual) {
                        documentenUris.add(documentUrlNode.textValue())
                    } else if (documentUrlNode.isArray) {
                        documentUrlNode.forEach { documentenUris.add(it.textValue()) }
                    } else {
                        throw NotificatiesNotificationEventException(
                            "Could not retrieve document Urls. Found invalid URL in '/documenten'. ${documentUrlNode.toPrettyString()}"
                        )
                    }
                } catch (e: MalformedURLException) {
                    throw NotificatiesNotificationEventException(
                        "Could not retrieve document Urls. Malformed URL in: '/documenten'"
                    )
                }
            }
        }
        return documentenUris
    }

    internal fun startProcessToUploadDocuments(
        taakObject: TaakObject,
        processDefinitionKey: String,
        businessKey: String,
        objectenApiPluginConfigurationId: String,
        portaalTaakObjectUrl: String
    ) {
        logger.debug { "Starting process to upload documents for taak with verwerker task id '${taakObject.verwerkerTaakId}'" }
        val variables = mapOf(
            "portaalTaakObjectUrl" to portaalTaakObjectUrl,
            "objectenApiPluginConfigurationId" to objectenApiPluginConfigurationId,
            "verwerkerTaakId" to taakObject.verwerkerTaakId,
            "documentUrls" to getDocumentenUrls(objectMapper.valueToTree(taakObject.verzondenData))
        )
        try {
            runWithoutAuthorization {
                processService.startProcess(processDefinitionKey, businessKey, variables)
            }
            logger.info { "Process started successfully for process definition key '$processDefinitionKey' and document id '${businessKey}'" }
        } catch (ex: RuntimeException) {
            throw NotificatiesNotificationEventException(
                "Could not start process with definition: $processDefinitionKey and businessKey: $businessKey.\n " +
                    "Reason: ${ex.message}"
            )
        }
    }

    internal fun getPortaalTaakObjectData(
        objectManagement: ObjectManagement,
        event: NotificatiesApiNotificationReceivedEvent
    ): JsonNode {
        logger.debug { "Retrieving portaalTaak object data for event with resource url '${event.resourceUrl}'" }
        val objectenApiPlugin =
            pluginService
                .createInstance(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)) as ObjectenApiPlugin
        return objectenApiPlugin.getObject(URI(event.resourceUrl)).record.data
            ?: throw NotificatiesNotificationEventException(
                "Portaaltaak meta data was empty!"
            )
    }


    companion object {
        private val logger = KotlinLogging.logger {}
    }
}