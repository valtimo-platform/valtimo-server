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

package com.ritense.processdocument.service

import com.ritense.document.domain.impl.JsonDocumentContent
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition
import com.ritense.document.domain.impl.JsonSchemaDocumentId
import com.ritense.document.service.DocumentService
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.processdocument.BaseTest
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.contract.OauthConfigHolder
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.authentication.model.ValtimoUserBuilder
import com.ritense.valtimo.contract.config.ValtimoProperties.Oauth
import com.ritense.valtimo.contract.json.MapperSingleton
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID


internal class DocumentDelegateServiceTest : BaseTest() {

    private lateinit var processDocumentService: ProcessDocumentService
    private lateinit var documentService: DocumentService
    private lateinit var jsonSchemaDocumentService: JsonSchemaDocumentService
    private lateinit var userManagementService: UserManagementService
    private lateinit var documentDelegateService: DocumentDelegateService

    lateinit var definition: JsonSchemaDocumentDefinition
    private lateinit var delegateExecutionFake: DelegateExecution

    private val documentId = "11111111-1111-1111-1111-111111111111"
    private val processInstanceId = "00000000-0000-0000-0000-000000000000"

    private val documentMock = mock<JsonSchemaDocument>()
    private val jsonSchemaDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(documentId))

    companion object {
        private const val STREET_NAME = "street"
        private const val HOUSE_NUMBER = "3"
        private const val NO = false
    }

    @BeforeEach
    fun setup() {
        definition = definition()
        documentSequenceGeneratorService = mock()
        whenever(documentSequenceGeneratorService.next(any())).thenReturn(1L)
        processDocumentService = mock()
        userManagementService = mock()
        documentService = mock()
        jsonSchemaDocumentService = mock()
        jsonSchemaDocumentService = mock()
        documentDelegateService = DocumentDelegateService(
            processDocumentService,
            documentService,
            jsonSchemaDocumentService,
            userManagementService,
            MapperSingleton.get()
        )
        delegateExecutionFake =
            DelegateExecutionFake("id").withProcessBusinessKey("56f29315-c581-4c26-9b70-8bc818e8c86e")

        OauthConfigHolder(Oauth())
    }

    @Test
    fun `get modifiedOn from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val modifiedOn = LocalDateTime.now()

        whenever(documentMock.modifiedOn()).thenReturn(Optional.of(modifiedOn))
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val modifiedOnResult = documentDelegateService.getDocumentModifiedOn(delegateExecutionFake)

        assertEquals(modifiedOnResult, modifiedOn)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get assigneeId from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val assigneeId = "1234"

        whenever(documentMock.assigneeId()).thenReturn(assigneeId)
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val assigneeIdResult = documentDelegateService.getDocumentAssigneeId(delegateExecutionFake)

        assertEquals(assigneeIdResult, assigneeId)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get createdBy from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val createdBy = "Pietersen"

        whenever(documentMock.createdBy()).thenReturn(createdBy)
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val createdByResult = documentDelegateService.getDocumentCreatedBy(delegateExecutionFake)

        assertEquals(createdByResult, createdBy)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get fullname assignee from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val assigneeFullname = "Jan Jansen"

        whenever(documentMock.assigneeFullName()).thenReturn(assigneeFullname)
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val assigneFullNameResult = documentDelegateService.getDocumentAssigneeFullName(delegateExecutionFake)

        assertEquals(assigneFullNameResult, assigneeFullname)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get version from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val version = documentMock.version()

        whenever(documentMock.version()).thenReturn(version)
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val versionResult = documentDelegateService.getDocumentVersion(delegateExecutionFake)

        assertEquals(versionResult, version)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get createdOn from document`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)
        val createdOn = LocalDateTime.now()

        whenever(documentMock.createdOn()).thenReturn(createdOn)
        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val createdOnResult = documentDelegateService.getDocumentCreatedOn(delegateExecutionFake)

        assertEquals(createdOnResult, createdOn)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    @Test
    fun `get document by execution`() {
        val delegateExecutionFake = DelegateExecutionFake("id").withProcessInstanceId(processInstanceId)

        prepareDocument(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)

        val resultDocument = documentDelegateService.getDocument(delegateExecutionFake)

        assertEquals(documentMock, resultDocument)
        verifyTest(processDocumentService, delegateExecutionFake, jsonSchemaDocumentService)
    }

    private fun prepareDocument(processDocumentService: ProcessDocumentService,
                                delegateExecutionFake: DelegateExecutionFake,
                                jsonSchemaDocumentService: JsonSchemaDocumentService) {
        whenever(
            processDocumentService.getDocumentId(
                CamundaProcessInstanceId(processInstanceId),
                delegateExecutionFake
            )
        )
            .thenReturn(jsonSchemaDocumentId)

        whenever(jsonSchemaDocumentService.getDocumentBy(jsonSchemaDocumentId))
            .thenReturn(documentMock)
    }

    private fun verifyTest(processDocumentService: ProcessDocumentService,
                           delegateExecutionFake: DelegateExecutionFake,
                           jsonSchemaDocumentService: JsonSchemaDocumentService) {
        verify(processDocumentService).getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        verify(jsonSchemaDocumentService).getDocumentBy(jsonSchemaDocumentId)
    }

    @Test
    fun `find value by json pointer`() {
        val jsonSchemaDocument = createDocument()

        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any = documentDelegateService.findValueByJsonPointer(
            "/applicant/number", delegateExecutionFake
        )

        assertEquals(HOUSE_NUMBER, value)
    }

    @Test
    fun `incorrect path should return default value`() {
        val jsonSchemaDocument = createDocument()
        val defaultValue = "DEFAULT_VALUE"
        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any? = documentDelegateService.findValueByJsonPointerOrDefault(
            "/incorrectpath", delegateExecutionFake, defaultValue
        )

        assertEquals(defaultValue, value)
    }

    @Test
    fun `should accept null for default value`() {
        val jsonSchemaDocument = createDocument()
        val defaultValue = null
        whenever(documentService.findBy(any<JsonSchemaDocumentId>())).thenReturn(Optional.of(jsonSchemaDocument))
        val value: Any? = documentDelegateService.findValueByJsonPointerOrDefault(
            "/incorrectpath", delegateExecutionFake, defaultValue
        )

        assertEquals(defaultValue, value)
    }

    @Test
    fun `should assign user to document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId)
        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))
        whenever(userManagementService.findByEmail("john@example.com"))
            .thenReturn(Optional.of(ValtimoUserBuilder().id("anId").build()))

        documentDelegateService.setAssignee(delegateExecutionFake, "john@example.com")

        verify(documentService, times(1)).assignUserToDocument(UUID.fromString(documentId), "anId")
    }

    @Test
    fun `should set status to document`() {
        val documentId = JsonSchemaDocumentId.existingId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId.toString())
        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(documentId)

        val newStatus = "test"
        documentDelegateService.setInternalStatus(delegateExecutionFake, newStatus)

        verify(documentService).setInternalStatus(documentId, newStatus)
    }

    @Test
    fun `should unassign user from document`() {
        val documentId = "11111111-1111-1111-1111-111111111111"
        val processInstanceId = "00000000-0000-0000-0000-000000000000"
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessInstanceId(processInstanceId)
            .withProcessBusinessKey(documentId)
        whenever(
            processDocumentService.getDocumentId(CamundaProcessInstanceId(processInstanceId), delegateExecutionFake)
        ).thenReturn(JsonSchemaDocumentId.existingId(UUID.fromString(documentId)))

        documentDelegateService.unassign(delegateExecutionFake)

        verify(documentService, times(1)).unassignUserFromDocument(UUID.fromString(documentId))
    }


    private fun createDocument(): JsonSchemaDocument {
        return JsonSchemaDocument.create(
            definition, JsonDocumentContent(
                """
                {
                    "applicant": {
                        "street": "$STREET_NAME",
                        "number": "$HOUSE_NUMBER",
                        "prettyHouse": "$NO"
                    },
                    "cars":[
                        { "mark":"volvo", "year": 1991 },
                        { "mark":"audi", "year": 2016 }
                    ]
                }
            """.trimIndent()
            ),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument().get()
    }
}
