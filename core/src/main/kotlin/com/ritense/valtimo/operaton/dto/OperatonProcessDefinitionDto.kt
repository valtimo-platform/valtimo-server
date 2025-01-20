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

import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition

data class OperatonProcessDefinitionDto(
    var id: String?,
    var key: String?,
    var category: String?,
    var name: String?,
    var version: Int,
    var resource: String?,
    var deploymentId: String?,
    var diagram: String?,
    var suspended: Boolean,
    var tenantId: String?,
    var versionTag: String?,
    var historyTimeToLive: Int?,
    var isStartableInTasklist: Boolean,
) {

    companion object {

        @JvmStatic
        fun of(entity: OperatonProcessDefinition) = OperatonProcessDefinitionDto(
            id = entity.id,
            key = entity.key,
            category = entity.category,
            name = entity.name,
            version = entity.version,
            resource = entity.resourceName,
            deploymentId = entity.deploymentId,
            diagram = entity.diagramResourceName,
            suspended = entity.isSuspended(),
            tenantId = entity.tenantId,
            versionTag = entity.versionTag,
            historyTimeToLive = entity.historyTimeToLive,
            isStartableInTasklist = entity.isStartableInTasklist,
        )
    }
}
