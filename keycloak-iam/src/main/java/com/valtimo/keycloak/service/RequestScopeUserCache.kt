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

import com.ritense.valtimo.contract.authentication.ManageableUser
import com.ritense.valtimo.contract.authentication.model.ValtimoUser
import mu.KotlinLogging
import kotlin.reflect.KClass

open class RequestScopeUserCache(
    private val currentUserCache: MutableMap<CacheType, MutableMap<String, Any?>> = mutableMapOf(),
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> get(cacheType: CacheType, key: String, requestFunction: (String) -> T?): T? {
        if (currentUserCache[cacheType] == null) {
            currentUserCache[cacheType] = mutableMapOf()
        }
        if (!currentUserCache[cacheType]!!.containsKey(key)) {
            logger.debug { "Cache miss for $cacheType with key $key. Adding user to cache" }
            val newValue = requestFunction(key)
            if (newValue != null && !cacheType.cachedClass.java.isAssignableFrom(newValue!!::class.java)) {
                throw IllegalArgumentException("The type of the value returned by the request function (${newValue!!::class.java.name}) does not match the cache type (${cacheType.cachedClass.java.name})")
            }
            currentUserCache[cacheType]!!.put(key, newValue)

        }
        logger.debug { "Returning user information from cache $cacheType with key $key" }
        return currentUserCache[cacheType]?.get(key) as T?
    }

    enum class CacheType(
        val cachedClass: KClass<*>
    ) {
        EMAIL(ManageableUser::class),
        USER_IDENTIFIER(ValtimoUser::class);
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}