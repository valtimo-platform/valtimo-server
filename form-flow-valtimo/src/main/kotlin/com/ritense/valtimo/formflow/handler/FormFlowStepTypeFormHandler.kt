/*
 *  Copyright 2015-2022 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.formflow.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.document.service.DocumentService
import com.ritense.form.domain.FormDefinition
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formflow.domain.definition.configuration.step.FormStepTypeProperties
import com.ritense.formflow.domain.instance.FormFlowStepInstance
import com.ritense.formflow.handler.FormFlowStepTypeHandler
import com.ritense.formflow.service.FormFlowObjectMapper
import com.ritense.formlink.service.impl.CamundaFormAssociationService

class FormFlowStepTypeFormHandler(
    private val formIoFormDefinitionService: FormIoFormDefinitionService,
    private val camundaFormAssociationService: CamundaFormAssociationService,
    private val documentService: DocumentService,
    private val objectMapper: FormFlowObjectMapper
) : FormFlowStepTypeHandler {

    override fun getType() = "form"

    override fun getMetadata(stepInstance: FormFlowStepInstance, additionalParameters: Map<String, Any>): JsonNode {
        val formDefinition = getFormDefinition(stepInstance)
        prefillWithAdditionalData(formDefinition, additionalParameters)
        prefillWithSubmissionData(formDefinition, stepInstance)
        return formDefinition.formDefinition
    }

    private fun getFormDefinition(stepInstance: FormFlowStepInstance): FormIoFormDefinition {
        val stepDefinitionType = stepInstance.definition.type
        assert(stepDefinitionType.name == getType())
        val formDefinitionName = (stepDefinitionType.properties as FormStepTypeProperties).definition
        return formIoFormDefinitionService.getFormDefinitionByName(formDefinitionName)
            .orElseThrow { IllegalStateException("No FormDefinition found by name $formDefinitionName") }
    }

    private fun prefillWithSubmissionData(formDefinition: FormDefinition, stepInstance: FormFlowStepInstance) {
        formDefinition.preFill(objectMapper.get().readTree(stepInstance.instance.getSubmissionDataContext()))
    }

    private fun prefillWithAdditionalData(
        formDefinition: FormIoFormDefinition,
        additionalParameters: Map<String, Any>
    ) {
        val documentId = additionalParameters["documentId"] as String?
        val taskInstanceId = additionalParameters["taskInstanceId"] as String?

        if (documentId == null) {
            return
        }

        val document = documentService.get(documentId)
        val documentContent = document.content().asJson() as ObjectNode

        camundaFormAssociationService.prefillProcessVariables(formDefinition, document)
        camundaFormAssociationService.prefillDataResolverFields(formDefinition, document, documentContent)

        if (taskInstanceId != null) {
            camundaFormAssociationService.prefillTaskVariables(formDefinition, taskInstanceId, documentContent)
        }
    }
}
