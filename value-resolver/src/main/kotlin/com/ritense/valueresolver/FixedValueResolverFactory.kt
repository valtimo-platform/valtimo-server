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

package com.ritense.valueresolver

import java.util.function.Function
import org.camunda.bpm.engine.delegate.VariableScope

/**
 * This resolver returns the requestedValue as the value.
 * It will do a best-effort of guessing the type of the given requestedValue before returning it.
 *
 * For instance, "true" will become the boolean <code>true</code>
 *
 * These requestedValues do not have a prefix
 */
class FixedValueResolverFactory(
    private val prefix: String = ""
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return prefix
    }

    override fun createResolver(
        processInstanceId: String,
        variableScope: VariableScope
    ): Function<String, Any?> {
        return createResolver()
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return createResolver()
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        val firstValue = values.iterator().next()
        throw RuntimeException("Can't save fixed value (unknown destination): {${firstValue.key} to ${firstValue.value}}")
    }

    private fun createResolver(): Function<String, Any?> {
        return Function { requestedValue->
            requestedValue.toBooleanStrictOrNull()
                ?: toLongOrNullSave(requestedValue)
                ?: toDoubleOrNullSave(requestedValue)
                ?:  if (prefix.isEmpty()) {
                        requestedValue
                    } else {
                        "$prefix:$requestedValue"
                    }
        }
    }

    private fun toLongOrNullSave(value: String): Long? {
        return if (value.length >= 2 && value[0] == '0') {
            null
        } else {
            value.toLongOrNull()
        }
    }

    private fun toDoubleOrNullSave(value: String): Double? {
        return if (value.length >= 2 && value[0] == '0' && value[1] != '.') {
            null
        } else {
            value.toDoubleOrNull()
        }
    }

}
