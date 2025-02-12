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

package com.ritense.objectenapi.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.client.dto.TypedObjectRequest
import java.net.URI
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ObjectRequest(
    val uuid: UUID?,
    val type: URI,
    val record: ObjectRecord
) {
    constructor(
        type: URI,
        record: ObjectRecord
    ) : this(null, type, record)

    companion object {
        fun toTyped(objectRequest: ObjectRequest): TypedObjectRequest<JsonNode> {
            return TypedObjectRequest(
                objectRequest.uuid,
                objectRequest.type,
                ObjectRecord.toTyped(objectRequest.record)
            )
        }
    }
}
