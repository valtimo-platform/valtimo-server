package com.valtimo.keycloak.autoconfigure

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

@AutoConfiguration
class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CaffeineCacheManager::class)
    fun caffeineCacheManager(
        caffeine: Caffeine<Any, Any>
    ) = CaffeineCacheManager().apply {
        this.setCaffeine(caffeine)
    }

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)

}