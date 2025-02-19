/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.processlink.domain

import com.fasterxml.jackson.annotation.JsonTypeName
import com.ritense.processlink.autodeployment.ProcessLinkDeployDto
import com.ritense.processlink.domain.AnotherTestProcessLink.Companion.PROCESS_LINK_TYPE

@JsonTypeName(PROCESS_LINK_TYPE)
data class AnotherTestProcessLinkDeployDto(
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    val anotherValue: String = "another-test"
) : ProcessLinkDeployDto {
    override val processLinkType: String
        get() = PROCESS_LINK_TYPE
}
