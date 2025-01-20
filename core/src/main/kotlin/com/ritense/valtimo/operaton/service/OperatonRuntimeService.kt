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

package com.ritense.valtimo.operaton.service

import com.ritense.authorization.Action
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.operaton.domain.OperatonIdentityLink
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.domain.OperatonVariableInstance
import com.ritense.valtimo.operaton.repository.OperatonIdentityLinkRepository
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceRepository
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceSpecificationHelper.Companion.NAME
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceSpecificationHelper.Companion.byNameIn
import com.ritense.valtimo.operaton.repository.OperatonVariableInstanceSpecificationHelper.Companion.byProcessInstanceId
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@SkipComponentScan
class OperatonRuntimeService(
    private val runtimeService: RuntimeService,
    private val operatonVariableInstanceRepository: OperatonVariableInstanceRepository,
    private val operatonIdentityLinkRepository: OperatonIdentityLinkRepository,
    private val authorizationService: AuthorizationService
) {

    @Transactional(readOnly = true)
    fun findVariableInstances(
        specification: Specification<OperatonVariableInstance>,
        sort: Sort
    ): List<OperatonVariableInstance> {
        denyAuthorization()
        return operatonVariableInstanceRepository.findAll(specification, sort)
    }

    @Transactional(readOnly = true)
    fun getVariables(processInstanceId: String, variableNames: List<String>): Map<String, Any?> {
        denyAuthorization()

        val variableInstances = runWithoutAuthorization {
            findVariableInstances(
                byProcessInstanceId(processInstanceId).and(byNameIn(*variableNames.toTypedArray())),
                Sort.by(Sort.Direction.DESC, NAME)
            )
        }
        return variableInstances
            .filter { variableInstance: OperatonVariableInstance -> variableInstance.getValue() != null }
            .associate { obj -> obj.name to obj.getValue() }
    }

    @Transactional(readOnly = true)
    fun findProcessInstanceById(processInstanceId: String): ProcessInstance? {
        denyAuthorization()
        return runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult()
    }

    @Transactional(readOnly = true)
    fun getIdentityLink(identityLinkId: String): OperatonIdentityLink? {
        denyAuthorization()
        return operatonIdentityLinkRepository.findById(identityLinkId).orElse(null)
    }

    private fun denyAuthorization() {
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                OperatonProcessDefinition::class.java,
                Action.deny()
            )
        )
    }
}