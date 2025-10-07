package org.iot_platform.userservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/v1/users/register").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthConverter())
                }
            }
            .csrf { it.disable() }
            .build()
    }

    private fun jwtAuthConverter(): JwtAuthenticationConverter {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<SimpleGrantedAuthority>()

            // extarct scopes
            val scopeClaim = jwt.claims["scope"]
            val scopes = when (scopeClaim) {
                is String -> scopeClaim.split(" ").filter { it.isNotBlank() }
                is List<*> -> scopeClaim.filterIsInstance<String>()
                else -> emptyList()
            }
            scopes.forEach { authorities.add(SimpleGrantedAuthority("SCOPE_$it")) }

            // extract roles
            val realmAccess = jwt.claims["realm_access"]
            if (realmAccess is Map<*, *>) {
                val roles = (realmAccess["roles"] as? List<*>)
                    ?.filterIsInstance<String>()
                    .orEmpty()

                roles.forEach { authorities.add(SimpleGrantedAuthority("ROLE_$it")) }
            }

            // extract roles from resource_access
            val resourceAccess = jwt.getClaimAsMap("resource_access")
            if (resourceAccess is Map<*, *>) {
                resourceAccess.forEach { (clientId, clientRoles) ->
                    if (clientRoles is Map<*, *>) {
                        val roles = (clientRoles["roles"] as? List<*>)
                            ?.filterIsInstance<String>()
                            .orEmpty()
                        roles.forEach { authorities.add(SimpleGrantedAuthority("ROLE_${clientId}_$it")) }
                    }
                }
            }
            println("Extracted authorities: ${authorities.map { it.authority }}")
            authorities as Collection<GrantedAuthority>?
        }
        return jwtConverter
    }
}


