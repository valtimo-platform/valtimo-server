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

package com.ritense.zakenapi.service

import com.ritense.catalogiapi.service.CatalogiService
import com.ritense.documentenapi.DocumentenApiPlugin
import com.ritense.documentenapi.client.DocumentInformatieObject
import com.ritense.documentenapi.domain.DocumentenApiVersion
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.documentenapi.service.DocumentenApiVersionService
import com.ritense.documentenapi.service.DocumentenApiVersionService.Companion.MINIMUM_VERSION
import com.ritense.documentenapi.web.rest.dto.DocumentSearchRequest
import com.ritense.plugin.domain.PluginConfiguration
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZaakUrlProvider
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakInformatieObject
import com.ritense.zakenapi.domain.ZaakResponse
import com.ritense.zgw.Rsin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ZaakDocumentServiceTest {

    lateinit var service: ZaakDocumentService
    lateinit var zaakUrlProvider: ZaakUrlProvider
    lateinit var pluginService: PluginService
    lateinit var catalogiService: CatalogiService
    lateinit var documentenApiService: DocumentenApiService
    lateinit var documentenApiVersionService: DocumentenApiVersionService

    @BeforeEach
    fun init() {
        zaakUrlProvider = mock()
        pluginService = mock()
        catalogiService = mock()
        documentenApiService = mock()
        documentenApiVersionService = mock()
        service = ZaakDocumentService(
            zaakUrlProvider,
            pluginService,
            catalogiService,
            documentenApiService,
            documentenApiVersionService
        )
    }

    @Test
    fun `should get informatieobjecten for document`() {
        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/1")
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val zakenApiPlugin = mock<ZakenApiPlugin>()
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any()))
            .doReturn(zakenApiPlugin)


        val zaakInformatieObjects = createZaakInformatieObjecten(zaakUrl)
        whenever(zakenApiPlugin.getZaakInformatieObjecten(zaakUrl)).thenReturn(
            zaakInformatieObjects
        )

        val documentenApiPluginConfiguration = mock<PluginConfiguration>()
        val documentenApiPlugin = mock<DocumentenApiPlugin>()
        whenever(pluginService.findPluginConfiguration(eq(DocumentenApiPlugin::class.java), any()))
            .doReturn(documentenApiPluginConfiguration)
        whenever(documentenApiPluginConfiguration.id)
            .doReturn(PluginConfigurationId(UUID.randomUUID()))
        whenever(pluginService.createInstance(eq(documentenApiPluginConfiguration)))
            .doReturn(documentenApiPlugin)
        whenever(documentenApiPlugin.getInformatieObject(any<URI>())).doAnswer { answer ->
            val uri = answer.getArgument(0) as URI

            createDocumentInformatieObject(uri)
        }

        val relatedFiles = service.getInformatieObjectenAsRelatedFiles(documentId)

        assertEquals(5, relatedFiles.size)
        relatedFiles.forEachIndexed { index, relatedFile ->
            assertEquals(UUID.fromString("b059092c-9557-431a-9118-97f147903270"), relatedFile.fileId)
            assertEquals(documentenApiPluginConfiguration.id.id, relatedFile.pluginConfigurationId)
            assertEquals("http://localhost/informatieobjecttype", relatedFile.informatieobjecttype)
        }
    }

    @Test
    fun `should get zaak by document id`() {
        val documentId = UUID.randomUUID()
        val zaakId = UUID.randomUUID()
        val zaak = ZaakResponse(
            url = URI("http://localhost/$zaakId"),
            uuid = zaakId,
            bronorganisatie = Rsin("002564440"),
            zaaktype = URI("http://localhost/zaaktype"),
            verantwoordelijkeOrganisatie = Rsin("002564440"),
            startdatum = LocalDate.now()
        )
        doReturn(zaak.url).whenever(zaakUrlProvider).getZaakUrl(documentId)
        val zakenApiPlugin = mock<ZakenApiPlugin>()
        doReturn(zakenApiPlugin).whenever(pluginService).createInstance(eq(ZakenApiPlugin::class.java), any())
        doReturn(zaak).whenever(zakenApiPlugin).getZaak(zaak.url)

        val result = service.getZaakByDocumentId(documentId)

        assertEquals(zaak, result)
    }

    @Test
    fun `should get InformatieObjecten Page for zaak`() {
        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/1")
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val documentSearchRequestCaptor = argumentCaptor<DocumentSearchRequest>()
        val pageable = mock<Pageable>()
        val documentSearchRequest = DocumentSearchRequest()
        val resultPage = PageImpl(listOf<DocumentInformatieObject>())

        whenever(
            documentenApiService.getCaseInformatieObjecten(
                any(),
                documentSearchRequestCaptor.capture(),
                any()
            )
        ).thenReturn(resultPage)
        whenever(documentenApiVersionService.getVersionByDocumentId(documentId))
            .thenReturn(DocumentenApiVersion("1.5.0-test-1.0.0", listOf("titel"), listOf("titel")))

        val page = service.getInformatieObjectenAsRelatedFilesPage(documentId, documentSearchRequest, pageable)

        assertEquals(resultPage, page)
        // Check if the zaakUrl is set in the DocumentSearchRequest
        assertEquals(zaakUrl, documentSearchRequestCaptor.firstValue.zaakUrl)
    }

    @Test
    fun `should throw when get InformatieObjecten Page for zaak does not support filtering`() {
        val documentId = UUID.randomUUID()
        val zaakUrl = URI("https://example.com/1")
        whenever(zaakUrlProvider.getZaakUrl(documentId)).thenReturn(zaakUrl)

        val pageable = PageRequest.of(0, 10)
        val documentSearchRequest = DocumentSearchRequest(titel = "The Ritensions")

        whenever(documentenApiVersionService.getVersionByDocumentId(documentId)).thenReturn(MINIMUM_VERSION)
        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any()))
            .thenReturn(mock<ZakenApiPlugin>())

        val exception = assertThrows<IllegalStateException> {
            service.getInformatieObjectenAsRelatedFilesPage(documentId, documentSearchRequest, pageable)
        }

        assertEquals("Unsupported filter 'titel' on Documenten API with version 1.0.0", exception.message)
    }

    @Test
    fun `should delete all informatie objecten for zaak`() {
        val documentUrl = URI("http://localhost/zaak/1")
        val zaakApiPlugin = mock<ZakenApiPlugin>()

        whenever(pluginService.createInstance(eq(ZakenApiPlugin::class.java), any()))
            .thenReturn(zaakApiPlugin)
        val doc1 = mock<ZaakInformatieObject>()
        val doc2 = mock<ZaakInformatieObject>()

        whenever(zaakApiPlugin.getZaakInformatieObjecten(documentUrl)).thenReturn(listOf(doc1, doc2))

        whenever(doc1.informatieobject).thenReturn(URI("http://localhost/doc/1"))
        whenever(doc2.informatieobject).thenReturn(URI("http://localhost/doc/2"))

        service.deleteRelatedInformatieObjecten(documentUrl)

        val zaakDocumentCaptor = argumentCaptor<URI>()
        verify(documentenApiService, times(2)).deleteInformatieObject(zaakDocumentCaptor.capture())

        assertEquals(URI("http://localhost/doc/1"), zaakDocumentCaptor.firstValue)
        assertEquals(URI("http://localhost/doc/2"), zaakDocumentCaptor.secondValue)
    }

    private fun createZaakInformatieObjecten(zaakUrl: URI, count: Int = 5): List<ZaakInformatieObject> {
        return IntRange(0, count - 1)
            .map { index ->
                ZaakInformatieObject(
                    url = createUrl(zaakUrl, "/$index/f5abe5c3-a36c-485b-9935-407e69bae231"),
                    uuid = UUID.randomUUID(),
                    informatieobject = createUrl(zaakUrl, "/$index/b059092c-9557-431a-9118-97f147903270"),
                    zaak = zaakUrl,
                    aardRelatieWeergave = "...",
                    registratiedatum = LocalDateTime.now()
                )
            }
    }

    private fun createUrl(baseUrl: URI, path: String): URI {
        return URI("$baseUrl$path")
    }

    private fun createDocumentInformatieObject(uri: URI) = DocumentInformatieObject(
        url = uri,
        bronorganisatie = Rsin("404797441"),
        auteur = "y",
        beginRegistratie = LocalDateTime.now(),
        creatiedatum = LocalDate.now(),
        taal = "nl",
        titel = "titel",
        versie = 1,
        informatieobjecttype = "http://localhost/informatieobjecttype",
    )
}