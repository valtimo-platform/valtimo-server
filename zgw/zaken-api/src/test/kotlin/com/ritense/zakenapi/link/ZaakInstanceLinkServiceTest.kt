package com.ritense.zakenapi.link

import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class ZaakInstanceLinkServiceTest {

    val zaakInstanceLinkRepository = mock<ZaakInstanceLinkRepository>()

    val service = ZaakInstanceLinkService(zaakInstanceLinkRepository)

    @Test
    fun `getByDocumentId should throw exception when no link is found`() {
        val documentId = UUID.randomUUID()
        whenever(zaakInstanceLinkRepository.findByDocumentId(documentId)).thenReturn(null)

        val exception = assertThrows<ZaakInstanceLinkNotFoundException> {
            service.getByDocumentId(documentId)
        }

        assertEquals("No ZaakInstanceLink found for document id $documentId", exception.message)
    }
}