package com.valtimo.keycloak.service

import mu.KotlinLogging
import org.springframework.cache.caffeine.CaffeineCacheManager

class CacheManagerBasedUserCache(
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