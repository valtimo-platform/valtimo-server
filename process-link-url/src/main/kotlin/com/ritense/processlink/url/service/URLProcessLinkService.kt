package com.ritense.processlink.url.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.request.ModifyDocumentRequest
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.domain.ProcessDocumentDefinition
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.domain.request.Request
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.service.ProcessLinkService
import com.ritense.processlink.url.domain.URLVariables
import com.ritense.processlink.url.domain.URLProcessLink
import com.ritense.processlink.url.web.rest.dto.URLSubmissionResult
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import com.ritense.valtimo.camunda.service.CamundaRepositoryService
import java.util.UUID

class URLProcessLinkService(
    private val processLinkService: ProcessLinkService,
    private val documentService: JsonSchemaDocumentService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService,
    private val processDocumentService: ProcessDocumentService,
    private val repositoryService: CamundaRepositoryService,
    private val objectMapper: ObjectMapper,
    private val urlVariables: URLVariables
) {

    fun submit(
        processLinkId: UUID,
        documentDefinitionName: String?,
        documentId: String?,
        taskInstanceId: String?,
    ): URLSubmissionResult {
        val processLink = processLinkService.getProcessLink(processLinkId, URLProcessLink::class.java)
        val document = documentId
            ?.let { AuthorizationContext.runWithoutAuthorization { documentService.get(documentId) } }
        val processDefinition = getProcessDefinition(processLink)
        val documentDefinitionNameToUse = document?.definitionId()?.name()
            ?: documentDefinitionName
            ?: getProcessDocumentDefinition(processDefinition, document).processDocumentDefinitionId()
                .documentDefinitionId().name()

        val request = getRequest(
            processLink,
            document,
            taskInstanceId,
            documentDefinitionNameToUse,
            processDefinition.key
        )

        return dispatchRequest(
            request
        )
    }

    private fun getProcessDefinition(
        processLink: ProcessLink
    ): CamundaProcessDefinition {
        return AuthorizationContext.runWithoutAuthorization {
            repositoryService.findProcessDefinitionById(processLink.processDefinitionId)!!
        }
    }

    private fun getProcessDocumentDefinition(
        processDefinition: CamundaProcessDefinition,
        document: Document?
    ): ProcessDocumentDefinition {
        val processDefinitionKey = CamundaProcessDefinitionKey(processDefinition.key)
        return AuthorizationContext.runWithoutAuthorization {
            if (document == null) {
                processDocumentAssociationService.getProcessDocumentDefinition(processDefinitionKey)
            } else {
                processDocumentAssociationService.getProcessDocumentDefinition(
                    processDefinitionKey,
                    document.definitionId().version()
                )
            }
        }
    }

    private fun getRequest(
        processLink: URLProcessLink,
        document: Document?,
        taskInstanceId: String?,
        documentDefinitionName: String,
        processDefinitionKey: String,
    ): Request {
        return if (processLink.activityType == ActivityTypeWithEventName.START_EVENT_START) {
            if (document == null) {
                newDocumentAndStartProcessRequest(
                    documentDefinitionName,
                    processDefinitionKey
                )
            } else {
                modifyDocumentAndStartProcessRequest(
                    document,
                    processDefinitionKey
                )
            }
        } else if (processLink.activityType == ActivityTypeWithEventName.USER_TASK_CREATE) {
            modifyDocumentAndCompleteTaskRequest(
                document!!,
                taskInstanceId!!
            )
        } else {
            throw UnsupportedOperationException("Cannot handle submission for activity-type '" + processLink.activityType + "'")
        }
    }

    private fun newDocumentAndStartProcessRequest(
        documentDefinitionName: String,
        processDefinitionKey: String,
    ): NewDocumentAndStartProcessRequest {
        return NewDocumentAndStartProcessRequest(
            processDefinitionKey,
            NewDocumentRequest(
                documentDefinitionName,
                objectMapper.createObjectNode()
            )
        )
    }

    private fun modifyDocumentAndStartProcessRequest(
        document: Document,
        processDefinitionKey: String,
    ): ModifyDocumentAndStartProcessRequest {
        return ModifyDocumentAndStartProcessRequest(
            processDefinitionKey,
            ModifyDocumentRequest(
                document.id().toString(),
                objectMapper.createObjectNode()
            )
        )
    }

    private fun modifyDocumentAndCompleteTaskRequest(
        document: Document,
        taskInstanceId: String,
    ): ModifyDocumentAndCompleteTaskRequest {
        return ModifyDocumentAndCompleteTaskRequest(
            ModifyDocumentRequest(
                document.id().toString(),
                objectMapper.createObjectNode()
            ),
            taskInstanceId
        )
    }

    private fun dispatchRequest(
        request: Request
    ) : URLSubmissionResult {
        val result = processDocumentService.dispatch(request)
        return if (result.errors().isNotEmpty()) {
            URLSubmissionResult(result.errors().map { it.asString() }, "")
        } else {
            val submittedDocument = result.resultingDocument().orElseThrow()
            URLSubmissionResult(emptyList(), submittedDocument.id().toString())
        }
    }

    fun getVariables() = urlVariables

}