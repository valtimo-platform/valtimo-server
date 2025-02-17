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

package com.ritense.note.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.document.service.DocumentService
import com.ritense.note.BaseIntegrationTest
import com.ritense.note.domain.Note
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NoteSpecificationIT(
    @Autowired
    private val noteRepository: NoteRepository,

    @Autowired
    private val documentService: DocumentService,

    @Autowired
    private val objectMapper: ObjectMapper,

    @Autowired
    private val transactionManager: PlatformTransactionManager

): BaseIntegrationTest() {
    lateinit var documentId: UUID
    lateinit var noteId: UUID
    lateinit var note: Note
    lateinit var defaultTransactionDefinition: DefaultTransactionDefinition
    lateinit var readOnlyTransactionTemplate: TransactionTemplate

    @BeforeAll
    fun beforeEach() {

        defaultTransactionDefinition = DefaultTransactionDefinition().apply {
            propagationBehavior = DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW
            isReadOnly = true
        }

        readOnlyTransactionTemplate = TransactionTemplate(transactionManager, defaultTransactionDefinition)

        documentId = AuthorizationContext.runWithoutAuthorization {
            documentService.createDocument(
                NewDocumentRequest(PROFILE_DOCUMENT_DEFINITION_NAME, objectMapper.createObjectNode())
            ).resultingDocument().get().id()!!.id
        }

        noteId = UUID.randomUUID()
        note = Note(
            id = noteId,
            content = "Test",
            createdByUserId = "Test",
            createdByUserFullName = "Test",
            createdDate = LocalDateTime.now(),
            documentId = documentId
        )

        noteRepository.save(note)
    }

    @Test
    fun `should be able to get note by reference in read only transaction`() {

        // Given
        val noteSpecification = NoteSpecification(
            authRequest = mock(),
            permissions = listOf(),
            noteRepository = noteRepository,
            queryDialectHelper = mock()
        )

        // When
        val foundNote = readOnlyTransactionTemplate.execute { status ->
            assertTrue(status.isReadOnly)

            noteSpecification.javaClass.getDeclaredMethod("identifierToEntity", String::class.java)
                .invoke(noteSpecification, noteId.toString())
        }

        // Then
        assertEquals(note, foundNote)
    }

    companion object {
        private const val PROFILE_DOCUMENT_DEFINITION_NAME = "profile"
    }
 }