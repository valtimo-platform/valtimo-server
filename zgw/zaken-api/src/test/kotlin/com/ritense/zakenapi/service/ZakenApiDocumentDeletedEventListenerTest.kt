package com.ritense.zakenapi.service

import com.ritense.plugin.service.PluginService
import com.ritense.valtimo.contract.event.DocumentDeletedEvent
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.link.ZaakInstanceLinkNotFoundException
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.net.URI
import java.util.UUID

class ZakenApiDocumentDeletedEventListenerTest {

    private val zaakInstanceService = mock<ZaakInstanceLinkService>()
    private val zaakDocumentService = mock<ZaakDocumentService>()
    private val pluginService = mock<PluginService>()

    private val listener: ZakenApiDocumentDeletedEventListener = ZakenApiDocumentDeletedEventListener(zaakInstanceService, zaakDocumentService, pluginService)

    @Test
    fun `should delete zaak when one is linked`() {
        val documentId = UUID.fromString("d1f1b3ed-7575-45bb-a02b-18f378ddc34d")

        val zaakInstanceUrl = URI("http://zaaak.url")
        val zaakInstanceLink = mock<ZaakInstanceLink>()

        whenever(zaakInstanceService.getByDocumentId(documentId)).thenReturn(zaakInstanceLink)
        whenever(zaakInstanceLink.zaakInstanceUrl).thenReturn(zaakInstanceUrl)

        val pluginInstance = mock<ZakenApiPlugin>()
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any())).thenReturn(pluginInstance)

        listener.handle(DocumentDeletedEvent(documentId))

        verify(zaakDocumentService).deleteRelatedInformatieObjecten(zaakInstanceUrl)
        verify(pluginInstance).deleteZaak(zaakInstanceUrl)
    }

    @Test
    fun `should not throw exception when no zaak is linked`() {
        val documentId = UUID.fromString("d1f1b3ed-7575-45bb-a02b-18f378ddc34d")

        whenever(zaakInstanceService.getByDocumentId(documentId)).thenThrow(ZaakInstanceLinkNotFoundException("No link found"))

        listener.handle(DocumentDeletedEvent(documentId))

        verifyNoInteractions(zaakDocumentService)
        verifyNoInteractions(pluginService)
    }
}