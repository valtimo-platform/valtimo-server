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

package com.ritense.document.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.case.service.CaseDefinitionService
import com.ritense.document.domain.InternalCaseStatus
import com.ritense.document.domain.InternalCaseStatusId
import com.ritense.document.exception.InternalCaseStatusAlreadyExistsException
import com.ritense.document.exception.InternalCaseStatusNotFoundException
import com.ritense.document.repository.InternalCaseStatusRepository
import com.ritense.document.web.rest.dto.InternalCaseStatusCreateRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateOrderRequestDto
import com.ritense.document.web.rest.dto.InternalCaseStatusUpdateRequestDto
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Validated
@Transactional
@Service
@SkipComponentScan
class InternalCaseStatusService(
    private val internalCaseStatusRepository: InternalCaseStatusRepository,
    private val caseDefinitionService: CaseDefinitionService,
    private val authorizationService: AuthorizationService,
) {
    fun getInternalCaseStatuses(documentDefinitionName: String): List<InternalCaseStatus> {
        return internalCaseStatusRepository.findByIdCaseDefinitionKeyOrderByOrder(documentDefinitionName)
    }

    fun get(caseDefinitionName: String, statusKey: String): InternalCaseStatus {
        return internalCaseStatusRepository.getReferenceById(InternalCaseStatusId(caseDefinitionName, statusKey))
    }

    fun exists(caseDefinitionKey: String, statusKey: String): Boolean {
        return internalCaseStatusRepository.existsByIdCaseDefinitionKeyAndIdKey(caseDefinitionKey, statusKey)
    }

    fun create(
        caseDefinitionKey: String,
        @Valid request: InternalCaseStatusCreateRequestDto
    ): InternalCaseStatus {
        denyManagementOperation()

        caseDefinitionService.getLatestCaseDefinition(caseDefinitionKey)

        val currentInternalCaseStatuses = getInternalCaseStatuses(caseDefinitionKey)
        if (currentInternalCaseStatuses.any { status ->
                status.id.key == request.key
            }) {
            throw InternalCaseStatusAlreadyExistsException(request.key)
        }

        return internalCaseStatusRepository.save(
            InternalCaseStatus(
                InternalCaseStatusId(
                    caseDefinitionKey,
                    request.key
                ),
                request.title,
                request.visibleInCaseListByDefault,
                currentInternalCaseStatuses.size,
                request.color
            )
        )
    }

    fun update(
        caseDefinitionName: String,
        internalCaseStatusKey: String,
        @Valid request: InternalCaseStatusUpdateRequestDto,
    ) {
        denyManagementOperation()

        val oldInternalCaseStatus = internalCaseStatusRepository
            .findDistinctByIdCaseDefinitionKeyAndIdKey(
                caseDefinitionName, internalCaseStatusKey
            ) ?: throw InternalCaseStatusNotFoundException(internalCaseStatusKey, caseDefinitionName)

        internalCaseStatusRepository.save(
            oldInternalCaseStatus.copy(
                title = request.title,
                visibleInCaseListByDefault = request.visibleInCaseListByDefault,
                color = request.color
            )
        )
    }

    fun update(
        caseDefinitionName: String,
        @Valid requests: List<InternalCaseStatusUpdateOrderRequestDto>
    ): List<InternalCaseStatus> {
        denyManagementOperation()

        val existingInternalCaseStatuses = internalCaseStatusRepository
            .findByIdCaseDefinitionKeyOrderByOrder(caseDefinitionName)
        check(existingInternalCaseStatuses.size == requests.size) {
            throw IllegalStateException(
                "Failed to update internal case statuses. Reason: the number of internal "
                    + "case statuses in the update request does not match the number of existing internal case statuses."
            )
        }

        val updatedInternalCaseStatuses = requests.mapIndexed { index, request ->
            val existingInternalCaseStatus = existingInternalCaseStatuses.find { it.id.key == request.key }
                ?: throw InternalCaseStatusNotFoundException(request.key, caseDefinitionName)
            existingInternalCaseStatus.copy(
                title = request.title,
                order = index,
                visibleInCaseListByDefault = request.visibleInCaseListByDefault,
                color = request.color
            )
        }

        return internalCaseStatusRepository.saveAll(updatedInternalCaseStatuses)
    }

    fun delete(caseDefinitionName: String, internalCaseStatusKey: String) {
        denyManagementOperation()

        val internalCaseStatus =
            internalCaseStatusRepository.findDistinctByIdCaseDefinitionKeyAndIdKey(
                caseDefinitionName, internalCaseStatusKey
            ) ?: throw InternalCaseStatusNotFoundException(internalCaseStatusKey, caseDefinitionName)

        internalCaseStatusRepository.delete(internalCaseStatus)
        reorder(caseDefinitionName)
    }

    private fun reorder(caseDefinitionName: String) {
        val internalCaseStatuses = internalCaseStatusRepository.findByIdCaseDefinitionKeyOrderByOrder(
            caseDefinitionName
        ).mapIndexed { index, internalCaseStatus -> internalCaseStatus.copy(order = index) }
        internalCaseStatusRepository.saveAll(internalCaseStatuses)
    }

    private fun denyManagementOperation() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                Any::class.java,
                Action.deny()
            )
        )
    }
}