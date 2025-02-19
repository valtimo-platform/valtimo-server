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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY
import com.fasterxml.jackson.databind.node.JsonNodeType.BINARY
import com.fasterxml.jackson.databind.node.JsonNodeType.BOOLEAN
import com.fasterxml.jackson.databind.node.JsonNodeType.MISSING
import com.fasterxml.jackson.databind.node.JsonNodeType.NULL
import com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER
import com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT
import com.fasterxml.jackson.databind.node.JsonNodeType.POJO
import com.fasterxml.jackson.databind.node.JsonNodeType.STRING
import com.ritense.objectenapi.service.ZaakObjectConstants.Companion.ZAAKOBJECT_PREFIX
import com.ritense.objectenapi.service.ZaakObjectValueResolverFactory.Companion
import com.ritense.valtimo.contract.form.DataResolvingContext
import com.ritense.valtimo.contract.form.FormFieldDataResolver
import mu.KotlinLogging
import java.lang.Deprecated

@Deprecated(since = "11.0", forRemoval = true)
class ZaakObjectDataResolver(
    private val zaakObjectService: ZaakObjectService,
    private val objectMapper: ObjectMapper
): FormFieldDataResolver {

    override fun supports(externalFormFieldType: String): Boolean {
        return externalFormFieldType == ZAAKOBJECT_PREFIX
    }

    override fun get(
        dataResolvingContext: DataResolvingContext,
        vararg varNames: String
    ): MutableMap<String, Any?> {
        val results = mutableMapOf<String, Any?>()
        logger.debug { "Requested zaak object values '$varNames' for document ${dataResolvingContext.documentId}, document definition ${dataResolvingContext.documentDefinitionName}" }

        varNames.map {
            RequestedData(it)
        }.groupBy {
            it.objectType
        }.forEach{ objectTypeGroup ->
            val zaakObject = zaakObjectService.getZaakObjectOfTypeByName(
                dataResolvingContext.documentId, objectTypeGroup.key)
            val dataAsJsonNode = objectMapper.valueToTree<JsonNode>(zaakObject.record.data)
            objectTypeGroup.value.forEach {
                results[it.variableName] = extractValue(dataAsJsonNode.at(it.path))
            }
        }

        return results
    }

    private fun extractValue(node: JsonNode): Any? {
        return when(node.nodeType) {
            ARRAY -> node
            BINARY -> node.binaryValue()
            BOOLEAN -> node.booleanValue()
            MISSING -> null
            NULL -> null
            NUMBER -> node.asLong()
            OBJECT -> node
            POJO -> node
            STRING -> node.textValue()
            else -> null
        }
    }

    class RequestedData(
        val variableName: String,
    ) {
        val objectType = variableName.substringBeforeLast(":")
        val path = variableName.substringAfterLast(":")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}