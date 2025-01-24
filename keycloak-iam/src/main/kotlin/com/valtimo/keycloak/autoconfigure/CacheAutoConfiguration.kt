package com.valtimo.keycloak.autoconfigure

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import java.util.concurrent.TimeUnit

@AutoConfiguration
class CacheAutoConfiguration {

    @Bean
    @Order(408)
    @ConditionalOnMissingBean(CaffeineCacheManager::class)
    fun caffeineCacheManager(
        caffeine: Caffeine<Any, Any>
    ) = CaffeineCacheManager().apply {
        this.setCaffeine(caffeine)
    }

    @Bean
    @Order(410)
    @ConditionalOnMissingBean(CaffeineCacheManager::class)
    fun noOpCacheManager() = NoOpCacheManager()

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)

}