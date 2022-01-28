/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.smartdocuments.service

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.document.domain.Document
import com.ritense.document.service.DocumentService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.springframework.http.MediaType

class CamundaSmartDocumentGenerator(
    private val smartDocumentGenerator: SmartDocumentGenerator,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val documentService: DocumentService,
) {

    fun generate(execution: DelegateExecution, templateId: String, mediaTypeValue: String) {
        val document = getDocument(execution)
        val templateData = getTemplateData(execution, document)
        val mediaType = MediaType.valueOf(mediaTypeValue)
        smartDocumentGenerator.generateAndStoreDocument(document.id(), templateId, templateData, mediaType)
    }

    private fun getDocument(delegateExecution: DelegateExecution): Document {
        val processInstanceId = CamundaProcessInstanceId(delegateExecution.processInstanceId)
        val processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId)
        return if (processDocumentInstance.isPresent) {
            val jsonSchemaDocumentId = processDocumentInstance.get().processDocumentInstanceId().documentId()
            documentService.findBy(jsonSchemaDocumentId).orElseThrow()
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            documentService.get(delegateExecution.businessKey)
        }
    }

    private fun getTemplateData(execution: DelegateExecution, document: Document): MutableMap<String, Any> {
        val camundaPropertiesMap = mutableMapOf<String, Any>()
        execution
            .bpmnModelElementInstance
            .extensionElements
            .elementsQuery
            .filterByType(CamundaProperties::class.java)
            .singleResult()
            .camundaProperties
            .associateTo(camundaPropertiesMap) {
                it.camundaName to getPlaceholderValue(it.camundaValue, execution, document)
            }
        return camundaPropertiesMap
    }

    private fun getPlaceholderValue(value: String, execution: DelegateExecution, document: Document): Any {
        return if (value.startsWith("pv:")) {
            getPlaceholderValueFromProcessVariable(value.substring("pv:".length), execution)
        } else if (value.startsWith("doc:")) {
            getPlaceholderValueFromDocument(value.substring("doc:".length), document)
        } else {
            value
        }
    }

    private fun getPlaceholderValueFromProcessVariable(value: String, execution: DelegateExecution): Any {
        return execution.variables[value].toString()
    }

    private fun getPlaceholderValueFromDocument(value: String, document: Document): Any {
        val nodeOpt = document.content().getValueBy(JsonPointer.valueOf(value))
        return if (nodeOpt.isPresent) {
            nodeOpt.get().asText()
        } else {
            ""
        }
    }

}