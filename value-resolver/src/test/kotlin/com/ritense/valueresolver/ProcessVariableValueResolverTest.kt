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

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.operaton.bpm.engine.RuntimeService
import org.operaton.bpm.engine.delegate.DelegateTask
import org.operaton.bpm.engine.runtime.ProcessInstance
import org.operaton.bpm.engine.runtime.VariableInstance
import java.time.LocalDate
import java.util.UUID

internal class ProcessVariableValueResolverTest {
    private val runtimeService: RuntimeService = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val processVariableValueResolver = ProcessVariableValueResolverFactory(runtimeService)

    @Test
    fun `should resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val now = LocalDate.now()
        val variableScope = mock<DelegateTask> {
            on { getVariables() }.thenReturn(
                mapOf(
                    "firstName" to "John",
                    somePropertyName to true,
                    "lastName" to "Doe",
                    "dateTime" to now
                )
            )
        }

        val processInstanceId = UUID.randomUUID().toString()

        val resolver = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        )
        val somePropertyValue = resolver.apply(somePropertyName)
        val serializedValue = resolver.apply("dateTime")

        Assertions.assertThat(somePropertyValue).isEqualTo(true)
        Assertions.assertThat(serializedValue).isEqualTo(now)
    }

    @Test
    fun `should NOT resolve requestedValue from process variables`() {
        val somePropertyName = "somePropertyName"
        val variableScope = mock<DelegateTask> {
            on { getVariables() }.thenReturn(
                mapOf(
                    "firstName" to "John",
                    "lastName" to "Doe"
                )
            )
        }
        val processInstanceId = UUID.randomUUID().toString()

        val resolvedValue = processVariableValueResolver.createResolver(
            processInstanceId = processInstanceId,
            variableScope = variableScope
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isNull()
    }

    @Test
    fun `should resolve requestedValue from process variables by document ID`() {
        val somePropertyName = "somePropertyName"
        val documentInstanceId = UUID.randomUUID().toString()
        val processInstance = mock<ProcessInstance> {
            on { id }.thenReturn(UUID.randomUUID().toString())
        }
        whenever(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(documentInstanceId).list())
            .thenReturn(listOf(processInstance))

        val variableInstance = mock<VariableInstance> {
            on { name }.thenReturn(somePropertyName)
            on { value }.thenReturn(true)
        }
        whenever(runtimeService.createVariableInstanceQuery()
            .processInstanceIdIn(processInstance.id)
            .variableName(somePropertyName)
            .list())
            .thenReturn(listOf(variableInstance))

        val resolvedValue = processVariableValueResolver.createResolver(
            documentId = documentInstanceId
        ).apply(
            somePropertyName
        )

        Assertions.assertThat(resolvedValue).isEqualTo(true)
    }

    @Test
    fun `should handle value from process variables`() {
        val variableScope = mock<DelegateTask>()
        val processInstanceId = UUID.randomUUID().toString()

        processVariableValueResolver.handleValues(
            processInstanceId, variableScope, mapOf("firstName" to "John")
        )

        verify(runtimeService).setVariables(processInstanceId, mapOf("firstName" to "John"))
    }
}
