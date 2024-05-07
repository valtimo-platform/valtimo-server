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

package com.ritense.gzac.listener

import com.ritense.besluit.connector.BesluitProperties
import com.ritense.connector.domain.ConnectorType
import com.ritense.connector.service.ConnectorService
import com.ritense.contactmoment.connector.ContactMomentProperties
import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.objectsapi.opennotificaties.OpenNotificatieProperties
import com.ritense.objectsapi.productaanvraag.ProductAanvraagProperties
import com.ritense.objectsapi.productaanvraag.ProductAanvraagTypeMapping
import com.ritense.objectsapi.service.ObjectSyncService
import com.ritense.objectsapi.service.ObjectTypeConfig
import com.ritense.objectsapi.service.ObjectsApiProperties
import com.ritense.objectsapi.service.ServerAuthSpecification
import com.ritense.objectsapi.taak.TaakProperties
import com.ritense.objectsapi.web.rest.request.CreateObjectSyncConfigRequest
import com.ritense.openzaak.domain.configuration.Rsin
import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.domain.connector.OpenZaakProperties
import com.ritense.openzaak.service.InformatieObjectTypeLinkService
import com.ritense.openzaak.web.rest.request.CreateInformatieObjectTypeLinkRequest
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService
import com.ritense.zakenapi.service.ZaakTypeLinkService
import com.ritense.zakenapi.web.rest.request.CreateZaakTypeLinkRequest
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.net.URI
import java.util.UUID

@Component
class OpenZaakApplicationReadyEventListener(
    private val zaakTypeLinkService: ZaakTypeLinkService,
    private val documentDefinitionProcessLinkService: DocumentDefinitionProcessLinkService,
) {
    @EventListener(DocumentDefinitionDeployedEvent::class)
    fun handleDocumentDefinitionDeployed(event: DocumentDefinitionDeployedEvent) {
        connectZaakType(event)
    }

    private fun connectZaakType(event: DocumentDefinitionDeployedEvent) {
        if (event.documentDefinition().id().name() == "bezwaar") {
            zaakTypeLinkService.createZaakTypeLink(
                CreateZaakTypeLinkRequest(
                    "bezwaar",
                    URI(ZAAKTYPE_URL),
                    UUID.fromString(ZAKEN_API_PLUGIN_ID),
                    true,
                    "000000000"
                )
            )
        }
        if (event.documentDefinition().id().name() == PORTAL_PERSON) {
            zaakTypeLinkService.createZaakTypeLink(
                CreateZaakTypeLinkRequest(
                    PORTAL_PERSON,
                    URI(ZAAKTYPE_URL),
                    UUID.fromString(ZAKEN_API_PLUGIN_ID),
                    true,
                    RSIN
                )
            )
        }
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            PORTAL_PERSON,
            DocumentDefinitionProcessRequest("document-upload", "DOCUMENT_UPLOAD")
        )
        documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(
            "bezwaar",
            DocumentDefinitionProcessRequest("document-upload", "DOCUMENT_UPLOAD")
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ZAAKTYPE_URL = "http://localhost:8001/catalogi/api/v1/zaaktypen/744ca059-f412-49d4-8963-5800e4afd486"
        private const val PORTAL_PERSON = "portal-person"
        private const val ZAKEN_API_PLUGIN_ID = "3079d6fe-42e3-4f8f-a9db-52ce2507b7ee"
        private const val RSIN = "438605688"
    }
}
