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

package com.ritense.documentenapi.web.rest.dto

import com.ritense.documentenapi.domain.DocumentenApiColumn
import com.ritense.documentenapi.domain.DocumentenApiColumnId
import com.ritense.documentenapi.domain.DocumentenApiColumnKey

data class ColumnDto(
    val key: String,
    val enabled: Boolean,
) {
    fun toEntity(caseDefinitionName: String, order: Int = 0): DocumentenApiColumn = DocumentenApiColumn(
        id = DocumentenApiColumnId(caseDefinitionName, DocumentenApiColumnKey.valueOf(key.uppercase())),
        order = order,
        enabled = enabled
    )

    companion object {
        fun of(column: DocumentenApiColumn): ColumnDto = ColumnDto(
            key = column.id.key.name.lowercase(),
            enabled = column.enabled,
        )
    }
}