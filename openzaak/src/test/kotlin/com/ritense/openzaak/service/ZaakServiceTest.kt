/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service

import com.ritense.openzaak.BaseTest
import com.ritense.openzaak.domain.mapping.impl.ServiceTaskHandlers
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLink
import com.ritense.openzaak.domain.mapping.impl.ZaakTypeLinkId
import com.ritense.openzaak.service.impl.ZaakService
import com.ritense.openzaak.service.impl.model.ResultWrapper
import com.ritense.openzaak.service.impl.model.catalogi.Catalogus
import com.ritense.openzaak.service.impl.model.catalogi.InformatieObjectType
import com.ritense.openzaak.service.impl.model.catalogi.ZaakType
import com.ritense.openzaak.service.impl.model.zaak.Zaak
import com.ritense.zakenapi.domain.ZaakInstanceLink
import com.ritense.zakenapi.domain.ZaakInstanceLinkId
import org.assertj.core.api.Assertions.assertThat
import org.camunda.community.mockito.delegate.DelegateExecutionFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI
import java.time.Period
import java.util.UUID

class ZaakServiceTest : BaseTest() {

    private val UUID_STRING = "91e750e1-53ab-4922-9979-6a2dacd009cf"
    val zaakInstanceUrl = URI.create("http://zaak.instanceUrl.com")
    val zaaktypeLinkId = ZaakTypeLinkId.existingId(UUID.randomUUID())
    val zaakTypeUrl = URI.create("http://example.com")
    val catalogusUuid = UUID.randomUUID()
    val informatieobjecttype = URI.create("http://informatieobjecttypeUri.com")
    val informatieobjecttype1 = URI.create("http://informatieobjecttype2Uri.com")

    @BeforeEach
    fun setUp() {
        baseSetUp()
        whenever(zaakTypeService.getZaakType(anyOrNull())).thenReturn(
            ZaakType(
                zaakTypeUrl,
                "",
                "",
                Period.ofDays(1)
            )
        )

        whenever(zaakTypeLinkService.findBy(document.definitionId().name())).thenReturn(
            ZaakTypeLink(
                zaaktypeLinkId,
                "house",
                zaakTypeUrl,
                ServiceTaskHandlers()
            )
        )

        whenever(zaakInstanceLinkService.getByDocumentId(document.id!!.id)).thenReturn(
            ZaakInstanceLink(
                ZaakInstanceLinkId.newId(UUID.randomUUID()),
                zaakInstanceUrl,
                UUID.fromString(UUID_STRING),
                document.id!!.id,
                zaakTypeUrl
            )
        )

        zaakService = ZaakService(
            restTemplate,
            openZaakConfigService,
            openZaakTokenGeneratorService,
            zaakTypeLinkService,
            documentService,
            zaakInstanceLinkService,
            zaakTypeService
        )
    }

    @Test
    fun `should create zaak with link`() {
        //given
        val delegateExecutionFake = DelegateExecutionFake("id")
            .withProcessBusinessKey(document.id!!.id.toString())

        //when
        httpZaakCreated()

        zaakService.createZaakWithLink(delegateExecutionFake)

        //then
        verify(zaakInstanceLinkService).createZaakInstanceLink(
            zaakInstanceUrl,
            UUID.fromString(UUID_STRING),
            document.id!!.id,
            zaakTypeUrl
        )
    }

    @Test
    fun `should get list of informatieobjecttype`() {
        httpGetCatalogus()
        httpGetInformatieObjectTypen()

        val informatieobjecttypes = zaakService.getInformatieobjecttypes(catalogusUuid)

        assertThat(informatieobjecttypes).isNotEmpty
    }

    @Test
    fun `should get catalogus`() {
        httpGetCatalogus()

        val catalogus = zaakService.getCatalogus(catalogusUuid)

        assertThat(catalogus).isNotNull
        assertThat(catalogus.domein).isNotEmpty
        assertThat(catalogus.rsin).isNotEmpty
        assertThat(catalogus.informatieobjecttypen).contains(informatieobjecttype)
        assertThat(catalogus.informatieobjecttypen).contains(informatieobjecttype1)
    }

    private fun httpZaakCreated() {
        val responseEntity = ResponseEntity(
            createZaak(),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(
            restTemplate.exchange(
                contains("zaken/api/v1/zaken"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenReturn(responseEntity)
    }

    private fun createZaak(): Zaak {
        return Zaak(
            zaakInstanceUrl,
            UUID.fromString(UUID_STRING),
            "",
            "",
            "",
            "",
            URI.create("http://example.com"),
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            listOf(URI.create("http://example.com")),
            "",
            "",
            "",
            "",
            Zaak.Zaakgeometrie("", listOf(1)),
            Zaak.Verlenging("",""),
            Zaak.Opschorting(true,""),
            URI.create("http://example.com"),
            URI.create("http://example.com"),
            listOf(URI.create("http://example.com")),
            listOf(Zaak.RelevanteAndereZaken(URI.create("http://example.com"),"")),
            listOf(URI.create("http://example.com")),
            URI.create("http://example.com"),
            "",
            listOf(Zaak.Kenmerken("","")),
            "",
            "",
            "",
            URI.create("http://example.com"),
            ""
        )
    }

    private fun httpGetCatalogus() {
        val responseEntity = ResponseEntity(
            Catalogus(
                URI.create("http://catalogusUri.com"),
                "domein",
                "rsin",
                listOf(
                    informatieobjecttype,
                    informatieobjecttype1
                )
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(
            restTemplate.exchange(
                contains("catalogi/api/v1/catalogussen/"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenReturn(responseEntity)
    }

    private fun httpGetInformatieObjectTypen() {
        val responseEntity = ResponseEntity(
            ResultWrapper(
                1,
                null,
                null,
                listOf(
                    InformatieObjectType(
                        URI.create("http://url.com"),
                        "omschrijving"
                    )
                )
            ),
            httpHeaders(),
            HttpStatus.OK
        )
        whenever(
            restTemplate.exchange(
                contains("catalogi/api/v1/informatieobjecttypen"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                any(ParameterizedTypeReference::class.java)
            )
        ).thenReturn(responseEntity)
    }
}
