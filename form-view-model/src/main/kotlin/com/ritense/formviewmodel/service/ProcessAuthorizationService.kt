package com.ritense.formviewmodel.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.request.EntityAuthorizationRequest
import com.ritense.valtimo.operaton.authorization.OperatonExecutionActionProvider
import com.ritense.valtimo.operaton.domain.OperatonExecution
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition
import com.ritense.valtimo.operaton.service.OperatonRepositoryService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import org.operaton.bpm.engine.impl.persistence.entity.SuspensionState
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@SkipComponentScan
class ProcessAuthorizationService(
    private val operatonRepositoryService: OperatonRepositoryService,
    private val authorizationService: AuthorizationService
) {

    fun checkAuthorization(processDefinitionKey: String) {
        val processDefinition = runWithoutAuthorization {
            operatonRepositoryService.findLatestProcessDefinition(
                processDefinitionKey
            )
        }
        require(processDefinition != null)
        authorizationService.requirePermission(
            EntityAuthorizationRequest(
                OperatonExecution::class.java,
                OperatonExecutionActionProvider.CREATE,
                createDummyOperatonExecution(
                    processDefinition,
                    "UNDEFINED_BUSINESS_KEY"
                )
            )
        )
    }

    private fun createDummyOperatonExecution(
        processDefinition: OperatonProcessDefinition,
        businessKey: String
    ): OperatonExecution {
        val execution = OperatonExecution(
            id = UUID.randomUUID().toString(),
            revision = 1,
            rootProcessInstance = null,
            processInstance = null,
            businessKey = businessKey,
            parent = null,
            processDefinition = processDefinition,
            superExecution = null,
            superCaseExecutionId = null,
            caseInstanceId = null,
            activityId = null,
            activityInstanceId = null,
            active = true,
            concurrent = false,
            scope = false,
            eventScope = false,
            suspensionState = SuspensionState.ACTIVE.stateCode,
            cachedEntityState = 0,
            sequenceCounter = 0,
            tenantId = null,
            variableInstances = emptySet()
        );
        execution.processInstance = execution
        return execution
    }

}