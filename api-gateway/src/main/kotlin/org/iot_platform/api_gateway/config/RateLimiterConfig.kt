package org.iot_platform.api_gateway.config

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
class RateLimiterConfig {

    @Bean
    fun userKeyResolver(): KeyResolver {
        return KeyResolver { exchange ->
            val ipAddress = exchange.request.remoteAddress?.address?.hostAddress ?: "anonymous"
            Mono.just(ipAddress)
        }
    }

}