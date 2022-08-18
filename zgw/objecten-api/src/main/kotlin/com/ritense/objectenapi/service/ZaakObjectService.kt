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

package com.ritense.objectenapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objecttypenapi.ObjectType
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.plugin.service.PluginService
import com.ritense.zakenapi.ZakenApiPlugin
import com.ritense.zakenapi.domain.ZaakObject
import java.net.URI
import java.util.UUID

class ZaakObjectService(
    val zaakInstanceLinkService: ZaakInstanceLinkService,
    val pluginService : PluginService
) {
    fun getZaakObjectTypes(documentId: UUID): List<ObjectType> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl

        val zakenApiPluginInstance = pluginService
            .createInstanceConditional(ZakenApiPlugin::class.java) { properties: JsonNode ->
            zaakUrl.toString().startsWith(properties.get("url").textValue())
        }

        requireNotNull(zakenApiPluginInstance) { "No plugin configuration was found for zaak with URL $zaakUrl" }

        return zakenApiPluginInstance.getZaakObjecten(zaakUrl)
            .mapNotNull {
                getObjectByZaakObjectUrl(it)
            }.map { it.type }
            .distinct()
            .mapNotNull {
                getObjectTypeByUrl(it)
            }
    }

    private fun getObjectByZaakObjectUrl(zaakObject: ZaakObject) : ObjectWrapper? {
        val objectUrl = zaakObject.objectUrl
        val objectenApiPlugin = pluginService
            .createInstanceConditional(ObjectenApiPlugin::class.java) { properties: JsonNode ->
                objectUrl.toString().startsWith(properties.get("url").textValue())
            }?: return null
        return objectenApiPlugin.getObject(objectUrl)
    }

    private fun getObjectTypeByUrl(objectTypeUrl: URI): ObjectType? {
        val objectTypePluginInstance = pluginService
            .createInstanceConditional(ObjecttypenApiPlugin::class.java) { properties: JsonNode ->
                objectTypeUrl.toString().startsWith(properties.get("url").textValue())
            }?: return null

        return objectTypePluginInstance.getObjectType(objectTypeUrl)
    }

    fun getZaakObjecten(documentId: UUID, typeUrl: URI): List<Any> {
        val zaakUrl = zaakInstanceLinkService.getByDocumentId(documentId).zaakInstanceUrl
        //TODO: get plugin configuration for zaak objecten
        //TODO: get zaak object urls by zaakURL (zaakapi plugin)
        //TODO: get plugin configurations for objecten by urls
        //TODO: get zaak objecten by type urls (object api plugin)

        TODO()
    }

}