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

package com.ritense.search.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class DataType(@JsonValue val key: String) {
    TEXT("text"),
    NUMBER("number"),
    DATE("date"),
    DATETIME("datetime"),
    TIME("time"),
    BOOLEAN("boolean");

    companion object {
        /**
         * This creator allows for null, empty or non-matching keys to result in a null value.
         */
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun create(key: String?): DataType? {
            return entries.find { it.key.equals(key, true) }
        }
    }
}
