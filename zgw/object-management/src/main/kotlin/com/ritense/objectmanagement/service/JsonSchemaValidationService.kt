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

package com.ritense.objectmanagement.service

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion.VersionFlag
import com.networknt.schema.ValidationMessage


class JsonSchemaValidationService {

    fun validate(schema: JsonNode, data: JsonNode) {

        // This creates a schema factory that will use Draft 2020-12 as the default if $schema is not specified
        // in the schema data. If $schema is specified in the schema data then that schema dialect will be used
        // instead and this version is ignored.
        val jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012)
        val builder: SchemaValidatorsConfig.Builder = SchemaValidatorsConfig.builder()


        // By default the JDK regular expression implementation which is not ECMA 262 compliant is used
        // Note that setting this requires including optional dependencies
        // builder.regularExpressionFactory(GraalJSRegularExpressionFactory.getInstance());
        // builder.regularExpressionFactory(JoniRegularExpressionFactory.getInstance());
        val config: SchemaValidatorsConfig = builder.build()


        val schema: JsonSchema = jsonSchemaFactory.getSchema(schema, config)

        val validationMessages = schema.validate(data) { executionContext ->
            // By default since Draft 2019-09 the format keyword only generates annotations and not assertions
            executionContext.executionConfig.formatAssertionsEnabled = true
        }

        if (validationMessages.isNotEmpty()) {
            throw JsonSchemaValidationException(validationMessages)
        }
    }

    data class JsonSchemaValidationException(
        val messages: Set<ValidationMessage>
    ) : RuntimeException(
        messages.joinToString(prefix = "[\n", separator = ",\n", postfix = "]") { it.toString() },
    )
}