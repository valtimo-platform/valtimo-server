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
import com.ritense.objectenapi.client.typed.TypedObjectRecord
import java.time.LocalDate

class ObjectRecord(
    val index: Int? = null,
    val typeVersion: Int,
    val data: JsonNode? = null,
    val geometry: ObjectGeometry? = null,
    val startAt: LocalDate,
    val endAt: LocalDate? = null,
    val registrationAt: LocalDate? = null,
    val correctionFor: String? = null,
    val correctedBy: String? = null
) {
    companion object {
        fun ofTyped(typedObjectRecord: TypedObjectRecord<JsonNode>): ObjectRecord {
            return ObjectRecord(
                index = typedObjectRecord.index,
                typeVersion = typedObjectRecord.typeVersion,
                data = typedObjectRecord.data,
                geometry = typedObjectRecord.geometry,
                startAt = typedObjectRecord.startAt,
                endAt = typedObjectRecord.endAt,
                registrationAt = typedObjectRecord.registrationAt,
                correctionFor = typedObjectRecord.correctionFor,
                correctedBy = typedObjectRecord.correctedBy
            )
        }

        fun toTyped(objectRecord: ObjectRecord): TypedObjectRecord<JsonNode> {
            return TypedObjectRecord(
                index = objectRecord.index,
                typeVersion = objectRecord.typeVersion,
                data = objectRecord.data,
                geometry = objectRecord.geometry,
                startAt = objectRecord.startAt,
                endAt = objectRecord.endAt,
                registrationAt = objectRecord.registrationAt,
                correctionFor = objectRecord.correctionFor,
                correctedBy = objectRecord.correctedBy
            )
        }
    }
}

class ObjectGeometry(
    val type: String,
    val coordinates: Array<Int>
)