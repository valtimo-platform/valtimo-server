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

package com.ritense.verzoek

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.BaseIntegrationTest
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentService
import com.ritense.notificatiesapi.event.NotificatiesApiNotificationReceivedEvent
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.service.ObjectManagementService
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@Transactional
internal class VerzoekPluginEventListenerIntTest : BaseIntegrationTest() {

    private val bsn: String = "999999999"
    private val initiatoRolType: String = "https://example.com/my-role-type"
    private val rsin: String = "637549971"
    private val objectType: String = "anObjectType"
    private val zaakTypeUrl: String = "https://example.gov"
    private val verzoekObjectType = "objection"

    lateinit var documentDefinition: DocumentDefinition
    lateinit var notificatiesApiPluginConfiguration: PluginConfiguration
    lateinit var objectManagement: ObjectManagement

    @Autowired
    lateinit var verzoekPluginEventListener: VerzoekPluginEventListener

    @Autowired
    lateinit var objectManagementService: ObjectManagementService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var documentDefinitionService: DocumentDefinitionService

    @Autowired
    lateinit var processService: RuntimeService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var mockNotificatiesApi: MockWebServer

    @BeforeEach
    fun init() {
        notificatiesApiPluginConfiguration = mock()
        mockNotificatiesApi = MockWebServer()
        mockNotificatiesApi.start()

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
            [
              {
                "url": "http://example.com",
                "naam": "objecten",
                "documentatieLink": "http://example.com",
                "filters": [
                  "objecten"
                ]
              }
            ]
        """.trimIndent()
            )
        )

        mockNotificatiesApi.enqueue(
            mockResponse(
                """
                    {
                      "url": "http://example.com/abonnement/test-abonnement",
                      "callbackUrl": "http://example.com/callback",
                      "auth": "Bearer token",
                      "kanalen": [
                        {
                          "filters": {
                            "url": "http://example.com",
                            "someid": "1234"
                          },
                          "naam": "objecten"
                        }
                      ]
                    }
        """.trimIndent()
            )
        )

        documentDefinition = runWithoutAuthorization { documentDefinitionService.findLatestByName("profile").get() }

        objectManagement = objectManagementService.create(createObjectManagement())

        whenever(zaaktypeUrlProvider.getZaaktypeUrl(documentDefinition.id().name()))
            .thenReturn(URI.create(zaakTypeUrl))

        val notificatiesApiAuthenticationPluginConfiguration = createPluginConfiguration(
            "notificatiesapiauthentication", """
            {
              "clientId": "my-client-id",
              "clientSecret": "my-extra-long-client-secret-128370192641209486239846"
            }
        """.trimIndent()
        )

        notificatiesApiPluginConfiguration = createPluginConfiguration(
            "notificatiesapi", """
            {
              "url": "${mockNotificatiesApi.url("/api/v1/").toUri()}",
              "callbackUrl": "https://example.com/my-callback-api-endpoint",
              "authenticationPluginConfiguration": "${notificatiesApiAuthenticationPluginConfiguration.id.id}"
            }
        """.trimIndent()
        )
    }

    @AfterEach
    fun tearDown() {
        mockNotificatiesApi.shutdown()
    }

    @Test
    fun `should start process after receiving notification`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "specified",
                "mapping": [{
                    "target": "doc:/fullname",
                    "source": "/name"
                }]
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())

        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val event = createEvent()
        verzoekPluginEventListener.createZaakFromNotificatie(event)

        //assertions
        val processList = processService.createProcessInstanceQuery().processDefinitionKey("verzoek-process").list()
        assertEquals(1, processList.size)
        val processVariableMap =
            processService.createVariableInstanceQuery()
                .processInstanceIdIn(processList[0].id).list().associate { it.name to it.value }
        assertEquals(rsin, processVariableMap["RSIN"])
        assertEquals(zaakTypeUrl, processVariableMap["zaakTypeUrl"])
        assertEquals(initiatoRolType, processVariableMap["rolTypeUrl"])
        assertEquals(event.resourceUrl, processVariableMap["verzoekObjectUrl"])
        assertEquals("bsn", processVariableMap["initiatorType"])
        assertEquals(bsn, processVariableMap["initiatorValue"])

        val documentInstance = runWithoutAuthorization { documentService.get(processList[0].businessKey) }
        assertEquals(
            "John Doe",
            documentInstance.content().getValueBy(JsonPointer.valueOf("/fullname")).get().textValue()
        )
    }

    @Test
    fun `should support copying the root object in the Verzoek mapping`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "specified",
                "mapping": [{
                    "source": "",
                    "target": "doc:/verzoekObject"
                }]
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())

        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val event = createEvent()
        verzoekPluginEventListener.createZaakFromNotificatie(event)

        //assertions
        val processList = processService.createProcessInstanceQuery().processDefinitionKey("verzoek-process").list()
        assertEquals(1, processList.size)
        val processVariableMap =
            processService.createVariableInstanceQuery()
                .processInstanceIdIn(processList[0].id).list().associate { it.name to it.value }
        assertEquals(rsin, processVariableMap["RSIN"])
        assertEquals(zaakTypeUrl, processVariableMap["zaakTypeUrl"])
        assertEquals(initiatoRolType, processVariableMap["rolTypeUrl"])
        assertEquals(event.resourceUrl, processVariableMap["verzoekObjectUrl"])
        assertEquals("bsn", processVariableMap["initiatorType"])
        assertEquals(bsn, processVariableMap["initiatorValue"])

        val documentInstance = runWithoutAuthorization { documentService.get(processList[0].businessKey) }
        assertEquals(
            "John Doe",
            documentInstance.content().getValueBy(JsonPointer.valueOf("/verzoekObject/name")).get().textValue()
        )
    }

    @Test
    fun `should add the verzoek data to the process variables`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "specified",
                "mapping": [{
                    "target": "doc:/email",
                    "source": "/email"
                },
                {
                    "target": "pv:/fullname",
                    "source": "/name"
                },
                {
                    "target": "pv:informatieobjecttype",
                    "source": "object:/type"
                }]
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())

        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val event = createEvent()
        verzoekPluginEventListener.createZaakFromNotificatie(event)

        //assertions
        val processList = processService.createProcessInstanceQuery().processDefinitionKey("verzoek-process").list()
        assertEquals(1, processList.size)
        val processVariableMap =
            processService.createVariableInstanceQuery()
                .processInstanceIdIn(processList[0].id).list().associate { it.name to it.value }
        assertThat(processVariableMap).hasSize(14)
        assertThat(processVariableMap).containsKey("fullname")
        assertThat(processVariableMap).doesNotContainKey("email")
        assertEquals("John Doe", processVariableMap["fullname"])
        assertEquals("objection", processVariableMap["type"])
        assertEquals("999999999", processVariableMap["bsn"])
        assertEquals("http://localhost:8011/api/v1/objecttypes/107f0b7d-c6f7-4269-85e1-62003310230b", processVariableMap["informatieobjecttype"])
        assertEquals("[https://example-document-url.com/]", processVariableMap["attachments"].toString())
    }

    @Test
    fun `should throw exception when data is not present on retrieved object`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "full"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = false, verzoekObjectType, true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("VerzoekObject /record/data cannot be found!", exception.message)
    }

    @Test
    fun `should throw exception when types do not match`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "full"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = true, "otherType", true)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("Failed to find verzoek configuration of type otherType. For object aResource", exception.message)
    }

    @Test
    fun `should throw exception when object data is not present`() {
        createPluginConfiguration(
            "verzoek",
            """
            {
              "notificatiesApiPluginConfiguration": "${notificatiesApiPluginConfiguration.id.id}",
              "processToStart": "verzoek-process",
              "rsin": "$rsin",
              "verzoekProperties": [{
                "type": "objection",
                "caseDefinitionName": "${documentDefinition.id().name()}",
                "processDefinitionKey": "objection-process",
                "objectManagementId": "${objectManagement.id}",
                "initiatorRoltypeUrl": "$initiatoRolType",
                "initiatorRolDescription": "Initiator",
                "copyStrategy": "full"
              }]
            }
            """.trimIndent()
        )
        //mocks
        val mockObjectenApiPlugin = mock<ObjectenApiPlugin>()
        doCallRealMethod().whenever(pluginService).createInstance(any<Class<VerzoekPlugin>>(), any())
        doReturn(mockObjectenApiPlugin).whenever(pluginService)
            .createInstance(eq(PluginConfigurationId(objectManagement.objectenApiPluginConfigurationId)))
        doReturn(createObjectWrapper(withMetaData = true, verzoekObjectType, false)).whenever(mockObjectenApiPlugin)
            .getObject(any())
        //tested method
        val exception = assertThrows<RuntimeException> {
            verzoekPluginEventListener.createZaakFromNotificatie(createEvent())
        }
        //assertions
        assertEquals("VerzoekObject /record/data/data cannot be found! For verzoek with type 'objection'", exception.message)
    }

    private fun createObjectWrapper(withMetaData: Boolean, withType: String, withObjectData: Boolean): ObjectWrapper {
        return ObjectWrapper(
            url = URI.create(""),
            uuid = UUID.randomUUID(),
            type = URI.create("http://localhost:8011/api/v1/objecttypes/107f0b7d-c6f7-4269-85e1-62003310230b"),
            record = createRecord(withMetaData, withType, withObjectData)
        )
    }

    private fun createRecord(withMetaData: Boolean, withType: String, withObjectData: Boolean): ObjectRecord {
        return ObjectRecord(
            typeVersion = 1,
            data = if (withMetaData) objectMapper.readTree(
                """
                {
                    "type": "$withType",
                    ${createObjectData(withObjectData)}
                    "attachments": ["https://example-document-url.com/"],
                    "bsn": "$bsn"
                }
            """.trimIndent()
            ) else null,
            startAt = LocalDate.now()
        )
    }

    private fun createObjectData(withObjectData: Boolean): String {
        return if (withObjectData)
            """
                    "data": {
                          "name": "John Doe"
                    },
                """.trimIndent()
        else
            ""
    }


    private fun createPluginConfiguration(pluginDefinitionKey: String, pluginProperties: String): PluginConfiguration {
        return pluginService.createPluginConfiguration(
            "my-configuration-$pluginDefinitionKey-${pluginProperties.hashCode()}",
            objectMapper.readTree(pluginProperties).deepCopy(),
            pluginDefinitionKey
        )
    }

    private fun createObjectManagement(): ObjectManagement {
        return ObjectManagement(
            id = UUID.randomUUID(),
            title = "request",
            objecttypenApiPluginConfigurationId = UUID.randomUUID(),
            objecttypeId = objectType,
            objectenApiPluginConfigurationId = UUID.randomUUID(),
            showInDataMenu = false
        )
    }

    private fun mockResponse(body: String): MockResponse {
        return MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(body)
    }

    private fun createEvent(): NotificatiesApiNotificationReceivedEvent {
        return NotificatiesApiNotificationReceivedEvent(
            kanaal = "objecten",
            actie = "create",
            resourceUrl = "aResource",
            kenmerken = mapOf(Pair("objectType", "something/$objectType"))
        )
    }
}