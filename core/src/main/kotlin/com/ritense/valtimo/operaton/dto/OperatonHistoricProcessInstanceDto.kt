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

package com.ritense.valtimo.operaton.dto

import com.ritense.valtimo.operaton.domain.OperatonHistoricProcessInstance
import java.time.LocalDateTime

data class OperatonHistoricProcessInstanceDto(
    val id: String?,
    val businessKey: String?,
    val processDefinitionId: String?,
    val processDefinitionKey: String?,
    val processDefinitionName: String?,
    val processDefinitionVersion: Int?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val removalTime: LocalDateTime?,
    val durationInMillis: Long?,
    val startUserId: String?,
    val startActivityId: String?,
    val deleteReason: String?,
    val rootProcessInstanceId: String?,
    val superProcessInstanceId: String?,
    val superCaseInstanceId: String?,
    val caseInstanceId: String?,
    val tenantId: String?,
    val state: String?,
) {

    companion object {

        @JvmStatic
        fun of(entity: OperatonHistoricProcessInstance) = OperatonHistoricProcessInstanceDto(
            entity.id,
            entity.businessKey,
            entity.getProcessDefinitionId(),
            entity.processDefinitionKey,
            entity.processDefinition?.name,
            entity.processDefinition?.version,
            entity.startTime,
            entity.endTime,
            entity.removalTime,
            entity.durationInMillis,
            entity.startUserId,
            entity.startActivityId,
            entity.deleteReason,
            entity.rootProcessInstanceId,
            entity.superProcessInstanceId,
            entity.superCaseInstanceId,
            entity.caseInstanceId,
            entity.tenantId,
            entity.state,
        )
    }
}
