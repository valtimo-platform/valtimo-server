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

package com.ritense.besluit.client

import com.ritense.besluit.connector.BesluitProperties
import com.ritense.besluit.domain.Besluit
import com.ritense.besluit.domain.request.CreateBesluitRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

open  class BesluitClient(
    private val besluitWebClient: WebClient,
    private val besluitTokenGenerator: BesluitTokenGenerator,
    private val besluitProperties: BesluitProperties
) {
    /**
     * Create a BESLUIT
     *
     * @param request the <code>CreateBesluitRequest</code> to use when createing new requests
     */
    suspend fun createBesluit(request: CreateBesluitRequest): Besluit {
        return webClient()
            .post()
            .uri("/api/v1/besluiten/besluiten")
            .bodyValue(request)
            .retrieve()
            .awaitBody()
    }

    private fun webClient(): WebClient {
        val token = besluitTokenGenerator.generateToken(
            besluitProperties.besluitApi.secret,
            besluitProperties.besluitApi.clientId
        )
        return besluitWebClient
            .mutate()
            .baseUrl(besluitProperties.besluitApi.url)
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }
}