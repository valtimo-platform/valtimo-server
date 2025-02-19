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

package com.ritense.case.web.rest.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.exception.InvalidListColumnException
import org.zalando.problem.Status

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CaseListColumnDto(
    var title: String?,
    var key: String,
    var path: String,
    var displayType: DisplayType,
    var sortable: Boolean,
    var defaultSort: ColumnDefaultSort?,
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var order: Int?
) {
    @Throws(InvalidListColumnException::class)
    fun validate() {
        if (!displayType.displayTypeParameters.validate()) {
            throw InvalidListColumnException(
                "Display type parameters are invalid for type ${displayType.type}.",
                Status.BAD_REQUEST
            )
        }
    }
}
