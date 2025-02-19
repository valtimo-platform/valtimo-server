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

package com.ritense.document.web.rest.dto

import com.ritense.document.domain.CaseTag
import com.ritense.document.domain.CaseTagColor

data class CaseTagResponseDto(
    val key: String,
    val caseDefinitionName: String,
    val title: String,
    val color: CaseTagColor,
    val order: Int,
    val visibleInCaseListByDefault: Boolean

) {
    constructor(caseTag: CaseTag) : this(
        caseTag.id.key,
        caseTag.id.caseDefinitionName,
        caseTag.title,
        caseTag.color,
        1,
        true
    )
}