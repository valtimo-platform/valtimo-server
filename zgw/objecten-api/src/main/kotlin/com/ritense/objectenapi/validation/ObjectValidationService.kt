/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.objectenapi.validation

import com.google.common.cache.CacheBuilder
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.objecttypenapi.client.ObjecttypeVersion
import com.ritense.plugin.service.PluginService
import mu.KLogger
import mu.KotlinLogging
import java.net.URI
import java.time.Clock
import java.time.Duration
import java.time.Instant

class ObjectValidationService(
    private val pluginService: PluginService,
    private val jsonSchemaValidationService: JsonSchemaValidationService,
    private val validationType: ValidationType = ValidationType.LOG,
    private val cacheTtl: Duration = Duration.ofHours(1),
    private val clock: Clock = Clock.systemDefaultZone() //Can be used for testing or ticking on whole minutes
) {
    private val objectTypeCache = CacheBuilder.newBuilder()
        .build<URI, CachedObjectType>()

    fun validate(objectRequest: ObjectRequest, patch: Boolean = false) {
        if (validationType == ValidationType.DISABLED || objectRequest.record.data == null) {
            return
        }

        val plugin = getObjectTypenApiPlugin(objectRequest.type)!!
        val objectTypeUrl = objectRequest.type
        val objectTypeVersionUrl = plugin.getObjectTypeVersionUrl(objectTypeUrl, objectRequest.record.typeVersion)

        try {
            val objecttypeVersion = getObjectTypeVersion(plugin, objectTypeVersionUrl)

            jsonSchemaValidationService.validate(objecttypeVersion.jsonSchema, objectRequest.record.data, patch)
        } catch (e: Exception) {
            logger.error(e) { "Error while validating data for object of type $objectTypeVersionUrl" }
            if (validationType == ValidationType.ERROR) {
                throw e
            }
        }
    }

    private fun getObjectTypeVersion(
        plugin: ObjecttypenApiPlugin,
        objectTypeVersionUrl: URI
    ): ObjecttypeVersion {
        val cachedObjectType = objectTypeCache.get(objectTypeVersionUrl) {
            CachedObjectType(
                plugin.getObjecttypeVersion(objectTypeVersionUrl)
            )
        }

        val objecttypeVersion = cachedObjectType.objecttypeVersion

        // Published type versions are not invalidated from the cache, since they don't change anymore
        if (objecttypeVersion.status !== ObjecttypeVersion.Status.PUBLISHED &&
            cachedObjectType.createdOn.plus(cacheTtl).isAfter(Instant.now(clock))
        ) {
            objectTypeCache.invalidate(objectTypeVersionUrl)
        }

        return objecttypeVersion
    }

    private fun getObjectTypenApiPlugin(typeUrl:URI) = pluginService.createInstance(
            clazz = ObjecttypenApiPlugin::class.java,
            configurationFilter = ObjecttypenApiPlugin.findConfigurationByUrl(typeUrl)
        )

    private data class CachedObjectType(
        val objecttypeVersion: ObjecttypeVersion
    ) {
        val createdOn = Instant.now()
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }

}