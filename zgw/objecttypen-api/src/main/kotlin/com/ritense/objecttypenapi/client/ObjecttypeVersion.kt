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

package com.ritense.objecttypenapi.client

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import java.time.LocalDate

data class ObjecttypeVersion(
    val url: URI,
    val version: Int,
    val objectType: URI,
    val status: Status,
    val jsonSchema: JsonNode,
    val createdAt: LocalDate,
    val modifiedAt: LocalDate,
    val publishedAt: LocalDate?,
) {
    enum class Status(@JsonValue val jsonValue: String) {
        PUBLISHED("published"),
        DRAFT("draft"),
        DEPRECATED("deprecated"),
    }
}


