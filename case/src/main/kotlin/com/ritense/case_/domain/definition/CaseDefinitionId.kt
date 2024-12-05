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

package com.ritense.case_.domain.definition

import com.ritense.document.domain.InternalCaseStatusId
import com.ritense.valtimo.contract.domain.AbstractId
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CaseDefinitionId(
    @Column(name = "case_definition_key", nullable = false, updatable = false)
    val key: String,
    @Column(name = "case_definition_version_tag", nullable = false, updatable = true)
    val versionTag: String
) : AbstractId<CaseDefinitionId>() {
    init {
        require(key.isNotBlank()) { "[caseDefinitionId.key] was blank!" }
        require(key.matches(Regex("^[a-zA-Z0-9\\-]+$"))) {
            "[caseDefinitionId.key] contains characters that are not allowed (only alphanumeric characters and dashes)"
        }
        require(versionTag.isNotBlank()) { "[caseDefinitionId.version] tag was blank!" }
    }

    companion object {
        @JvmStatic
        fun of(key: String, versionTag: String): CaseDefinitionId {
            return CaseDefinitionId(key, versionTag)
        }
    }
}