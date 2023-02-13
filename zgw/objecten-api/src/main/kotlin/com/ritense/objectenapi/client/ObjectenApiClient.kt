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

package com.ritense.objectenapi.client

import com.ritense.objectenapi.ObjectenApiAuthentication
import java.net.URI
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

class ObjectenApiClient(
    val webclientBuilder: WebClient.Builder
) {

    fun getObject(
        authentication: ObjectenApiAuthentication,
        objectUrl: URI
    ): ObjectWrapper {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .get()
            .uri(objectUrl)
            .retrieve()
            .toEntity(ObjectWrapper::class.java)
            .block()

        return result?.body!!
    }

    fun getObjectsByObjecttypeUrl(
        authentication: ObjectenApiAuthentication,
        objecttypesApiUrl: URI,
        objectsApiUrl: URI,
        objectypeId: String,
        pageable: Pageable
    ): ObjectsList {
        val objectTypeUrl = UriComponentsBuilder.newInstance()
            .uri(objecttypesApiUrl)
            .pathSegment("objecttypes")
            .pathSegment(objectypeId)
            .toUriString()

        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .baseUrl(objectsApiUrl.toASCIIString())
            .build()
            .get()
            .uri { builder ->
                builder.path("objects")
                    .queryParam("type", objectTypeUrl)
                    .queryParam("pageSize", pageable.pageSize)
                    .queryParam("page", pageable.pageNumber + 1) //objects api pagination starts at 1 instead of 0
                    .build()
            }
            .header("Accept-Crs", "EPSG:4326")
            .retrieve()
            .toEntity(ObjectsList::class.java)
            .block()

        return result?.body!!
    }

    fun createObject(
        authentication: ObjectenApiAuthentication,
        objectsApiUrl: URI,
        objectRequest: ObjectRequest
    ): ObjectWrapper {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .baseUrl(objectsApiUrl.toASCIIString())
            .build()
            .post()
            .uri("objects")
            .header("Content-Crs", "EPSG:4326")
            .bodyValue(objectRequest)
            .retrieve()
            .toEntity(ObjectWrapper::class.java)
            .block()
        return result?.body!!
    }

    fun objectUpdate(
        authentication: ObjectenApiAuthentication,
        objectUrl: URI,
        objectRequest: ObjectRequest
    ): ObjectWrapper {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .put()
            .uri(objectUrl)
            .header("Content-Crs", "EPSG:4326")
            .bodyValue(objectRequest)
            .retrieve()
            .toEntity(ObjectWrapper::class.java)
            .block()

        return result?.body!!
    }

    fun deleteObject(authentication: ObjectenApiAuthentication, objectUrl: URI): HttpStatus {
        val result = webclientBuilder
            .clone()
            .filter(authentication)
            .build()
            .delete()
            .uri(objectUrl)
            .header("Content-Crs", "EPSG:4326")
            .retrieve()
            .toBodilessEntity()
            .block()

        return result?.statusCode!!
    }

}
