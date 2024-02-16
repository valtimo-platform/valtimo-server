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

package com.ritense.valtimo.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CustomLocalDateTimeDeserializer : LocalDateTimeDeserializer() {
    override fun _fromString(p: JsonParser, ctxt: DeserializationContext?, string0: String): LocalDateTime {
        val stringValue = string0.trim()
        if (stringValue.isEmpty()) {
            return _fromEmptyString(p, ctxt, stringValue)
        }
        return try {
            val result = DateTimeFormatter.ISO_DATE_TIME.parseBest(
                stringValue,
                ZonedDateTime::from,
                LocalDateTime::from
            )

            return when (result) {
                is LocalDateTime -> result
                is ZonedDateTime -> result.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                else -> super._fromString(p, ctxt, string0)
            }
        } catch (e: DateTimeException) {
            _handleDateTimeException(ctxt, e, stringValue)
        }
    }
}