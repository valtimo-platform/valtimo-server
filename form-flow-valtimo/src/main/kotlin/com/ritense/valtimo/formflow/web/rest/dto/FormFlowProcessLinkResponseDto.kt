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

package com.ritense.valtimo.formflow.web.rest.dto

import com.ritense.form.domain.FormDisplayType
import com.ritense.form.domain.FormSizes
import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.web.rest.dto.ProcessLinkResponseDto
import com.ritense.valtimo.formflow.mapper.FormFlowProcessLinkMapper.Companion.PROCESS_LINK_TYPE_FORM_FLOW
import java.util.UUID

data class FormFlowProcessLinkResponseDto(
    override val id: UUID,
    override val processDefinitionId: String,
    override val activityId: String,
    override val activityType: ActivityTypeWithEventName,
    override val processLinkType: String = PROCESS_LINK_TYPE_FORM_FLOW,
    val formFlowDefinitionId: String,
    val formDisplayType: FormDisplayType,
    val formSize: FormSizes,
    val subtitles: List<String>? = null
) : ProcessLinkResponseDto
