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

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.client.dto.TypedObjectWrapper
import java.net.URI
import java.util.UUID

data class ObjectWrapper(
    val url: URI,
    val uuid: UUID,
    val type: URI,
    val record: ObjectRecord
)

fun TypedObjectWrapper<JsonNode>.toObjectWrapper() = ObjectWrapper(
    url = url,
    uuid = uuid,
    type = type,
    record = record.toObjectRecord()
)
