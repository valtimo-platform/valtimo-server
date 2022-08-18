/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.objectenapi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectenApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals

internal class ObjectenApiPluginTest{

    val client = mock<ObjectenApiClient>()
    val plugin = ObjectenApiPlugin(client)

    @BeforeEach
    fun setUp() {
        plugin.authenticationPluginConfiguration = mock()
        plugin.url = mock()
    }

    @Test
    fun `should call client on get object`() {
        val objectUrl = URI("http://example.com")
        val objectMock = mock<ObjectWrapper>()
        whenever(client.getObject(plugin.authenticationPluginConfiguration, objectUrl)).thenReturn(objectMock)

        val result = plugin.getObject(objectUrl)

        assertEquals(objectMock, result)
        verify(client).getObject(any(), any())
    }

}