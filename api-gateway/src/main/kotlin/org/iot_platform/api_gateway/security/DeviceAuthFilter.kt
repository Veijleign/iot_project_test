package org.iot_platform.api_gateway.security

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class DeviceAuthFilter : GlobalFilter, Ordered {
    private val log = LoggerFactory.getLogger(DeviceAuthFilter::class.java)

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap { securityContext ->
                val authentication = securityContext.authentication
                if (authentication.authorities.any { it.authority == "ROLE_DEVICE" }) {
                    val deviceId = authentication.name
                    log.debug("Authenticated device: $deviceId")
                    val newRequest = exchange.request.mutate()
                        .header("X-Device-ID", deviceId)
                        .build()
                    chain.filter(exchange.mutate().request(newRequest).build())
                } else {
                    chain.filter(exchange)
                }
            }
    }

    override fun getOrder() = Ordered.HIGHEST_PRECEDENCE
}