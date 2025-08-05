package org.iot_platform.api_gateway.security

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class UserIdPropagationFilter : GlobalFilter, Ordered {
    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap { securityContext ->
                val principal = securityContext.authentication.principal
                if (principal is Jwt) {
                    val userId = principal.subject
                    val newRequest = exchange.request.mutate()
                        .header("X-User-ID", userId)
                        .build()
                    chain.filter(exchange.mutate().request(newRequest).build())
                } else {
                    chain.filter(exchange)
                }
            }
    }

    override fun getOrder() = Ordered.HIGHEST_PRECEDENCE
}