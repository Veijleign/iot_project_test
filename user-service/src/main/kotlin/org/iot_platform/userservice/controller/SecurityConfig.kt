package org.iot_platform.userservice.controller

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exhange ->
                exhange
                    // Healthchecks
                    .pathMatchers("/actuator/**").permitAll()

                    // Anything else - requiers authentication
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthConverter())
                }
            }
            .csrf { it.disable() }
            .build()
    }

    private fun jwtAuthConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<SimpleGrantedAuthority>()

            // extarct scopes
            val scopes = jwt.getClaimAsStringList("scope") ?: emptyList()
            scopes.forEach { scope ->
                authorities.add(SimpleGrantedAuthority("SCOPE_$scope"))
            }

            // extract roles
            val realmAccess = jwt.getClaimAsMap("realm_access")
            if (realmAccess != null) {
                val roles = realmAccess["roles"]
                if (roles is List<*>) {
                    roles.filterIsInstance<String>().forEach { role ->
                        authorities.add(SimpleGrantedAuthority("ROLE_$role"))
                    }
                }
            }

            // extract roles from resource_access
            val resourceAccess = jwt.getClaimAsMap("resource_access")
            resourceAccess?.forEach { (clientId, clientRoles) ->
                if (clientRoles is Map<*, *>) {
                    val roles = clientRoles["roles"]
                    if (roles is List<*>) {
                        roles.filterIsInstance<String>().forEach{ role ->
                            authorities.add(SimpleGrantedAuthority("ROLE_${clientId}_$role"))
                        }
                    }
                }
            }
            authorities as List<SimpleGrantedAuthority>
        }
        return ReactiveJwtAuthenticationConverterAdapter(jwtConverter)
    }
}


