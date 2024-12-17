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

package com.ritense.case.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.domain.TaskListColumnId
import com.ritense.case.exception.InvalidListColumnException
import com.ritense.case.exception.UnknownCaseDefinitionException
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.service.validations.ListColumnValidator
import com.ritense.case.service.validations.Operation
import com.ritense.case.service.validations.SaveTaskListColumnValidator
import com.ritense.case.web.rest.dto.TaskListColumnDto
import com.ritense.case.web.rest.mapper.TaskListColumnMapper
import com.ritense.case_.domain.definition.CaseDefinition
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.exception.UnknownDocumentDefinitionException
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valueresolver.ValueResolverService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.zalando.problem.Status
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
@SkipComponentScan
class TaskColumnService(
    private val taskListColumnRepository: TaskListColumnRepository,
    private val documentDefinitionService: DocumentDefinitionService,
    valueResolverService: ValueResolverService,
    private val authorizationService: AuthorizationService,
    private val caseDefinitionService: CaseDefinitionService
) {
    var validators: Map<Operation, ListColumnValidator<TaskListColumnDto>> = mapOf(
        Operation.CREATE to SaveTaskListColumnValidator(
            taskListColumnRepository,
            documentDefinitionService,
            valueResolverService
        )
    )

    @Throws(InvalidListColumnException::class)
    fun saveListColumn(
        caseDefinitionId: CaseDefinitionId,
        taskListColumnDto: TaskListColumnDto
    ) {
        denyManagementOperation()

        runWithoutAuthorization {
            validators[Operation.CREATE]!!.validate(caseDefinitionId, taskListColumnDto)
        }

        val originalColumn = taskListColumnRepository.findByIdCaseDefinitionIdAndIdKey(caseDefinitionId, taskListColumnDto.key)

        taskListColumnDto.order = originalColumn?.order ?: ((taskListColumnRepository.findMaxOrderByIdCaseDefinitionId(caseDefinitionId) ?: 0) + 1)

        taskListColumnRepository
            .save(TaskListColumnMapper.toEntity(caseDefinitionId, taskListColumnDto))
    }

    @Throws(InvalidListColumnException::class)
    fun swapColumnOrder(
        caseDefinitionId: CaseDefinitionId,
        taskColumnKey1: String,
        taskColumnKey2: String
    ) {
        denyManagementOperation()

        assertCaseDefinitionExists(caseDefinitionId)

        val columnsToSwap = taskListColumnRepository.findAllById(
            listOf(
                TaskListColumnId(caseDefinitionId, taskColumnKey1),
                TaskListColumnId(caseDefinitionId, taskColumnKey2)
            )
        )

        if (columnsToSwap.size != 2) {
            throw InvalidListColumnException(
                "Couldn't find the two columns to swap. Found ${columnsToSwap.size} columns",
                Status.BAD_REQUEST
            )
        }

        val col1Order = columnsToSwap[0].order
        columnsToSwap[0] = columnsToSwap[0].copy(order = columnsToSwap[1].order)
        columnsToSwap[1] = columnsToSwap[1].copy(order = col1Order)

        taskListColumnRepository.saveAll(columnsToSwap)
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun getListColumns(caseDefinitionId: CaseDefinitionId): List<TaskListColumnDto> {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.
        assertCaseDefinitionExists(caseDefinitionId)

        return TaskListColumnMapper
            .toDtoList(
                taskListColumnRepository.findByIdCaseDefinitionIdOrderByOrderAsc(
                    caseDefinitionId
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun getLatestListColumns(caseDefinitionName: String): List<TaskListColumnDto> {
        // TODO: Implement PBAC:
        // It currently relies on the VIEW check in findLatestByName via assertDocumentDefinitionExists.
        // Doing a check here forces this class to be a JsonSchemaDocument implementation, which is undesirable.

        val caseDefinition = assertLatestCaseDefinitionExists(caseDefinitionName)

        return TaskListColumnMapper
            .toDtoList(
                taskListColumnRepository.findByIdCaseDefinitionIdOrderByOrderAsc(
                    caseDefinition.id
                )
            )
    }

    @Throws(UnknownDocumentDefinitionException::class)
    fun deleteTaskListColumn(caseDefinitionId: CaseDefinitionId, columnKey: String) {
        denyManagementOperation()

        runWithoutAuthorization { assertCaseDefinitionExists(caseDefinitionId) }

        val taskListColumn = taskListColumnRepository.findByIdCaseDefinitionIdAndIdKey(caseDefinitionId, columnKey)
        if (taskListColumn != null) {
            taskListColumnRepository
                .decrementOrderDueToColumnDeletion(taskListColumn.id.caseDefinitionId, taskListColumn.order)

            taskListColumnRepository.delete(taskListColumn)
        }
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }

    @Throws(UnknownCaseDefinitionException::class)
    private fun assertLatestCaseDefinitionExists(caseDefinitionName: String): CaseDefinition {
        return caseDefinitionService.getLatestCaseDefinition(caseDefinitionName)
            ?: throw UnknownCaseDefinitionException(caseDefinitionName)
    }

    private fun assertCaseDefinitionExists(caseDefinitionId: CaseDefinitionId): CaseDefinition {
        return caseDefinitionService.getCaseDefinition(caseDefinitionId)
    }
}
