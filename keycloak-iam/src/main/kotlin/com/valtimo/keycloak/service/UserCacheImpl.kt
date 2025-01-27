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

package com.valtimo.keycloak.service

import mu.KotlinLogging
import org.springframework.cache.caffeine.CaffeineCacheManager

class UserCacheImpl(
    private val cacheManager: CaffeineCacheManager
) : UserCache {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(cacheType: CacheType, key: String, requestFunction: (String) -> T?): T? {
        cacheManager.getCache(cacheType.name)!!.let { cache ->
            if (cache.get(key) == null ) {
                logger.debug { "Cache miss for $cacheType with key $key. Adding user to cache" }
                requestFunction(key).let { newValue ->
                    if (newValue != null && !cacheType.cachedClass.java.isAssignableFrom(newValue!!::class.java)) {
                        throw IllegalArgumentException(
                            "The type of the value returned by the request function (${newValue!!::class.java.name}) " +
                                "does not match the cache type (${cacheType.cachedClass.java.name})"
                        )
                    }
                    cache.put(key, newValue)
                }
            }
            logger.debug { "Returning user information from cache $cacheType with key $key" }
            return cache.get(key)!!.get() as T?
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}