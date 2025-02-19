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

package com.ritense.zakenapi.link

import com.ritense.zakenapi.repository.ZaakInstanceLinkRepository
import org.junit.jupiter.api.Assertions.assertEquals
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