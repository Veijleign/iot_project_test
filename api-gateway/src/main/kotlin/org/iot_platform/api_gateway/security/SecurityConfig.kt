package org.iot_platform.api_gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchange ->
                exchange
                    // публичные эндпоинты
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/fallback").permitAll()

                    // Устройства - только для отправки телеметрии
                    .pathMatchers("/api/v1/telemetry/ingest")
                    .hasAnyAuthority("ROLE_iot-device", "SCOPE_device:telemetry")

                    // admin эндпоинты
                    .pathMatchers("/api/v1/devices/register").hasRole("ADMIN")
                    .pathMatchers("/api/v1/admin/**").permitAll()
                    .pathMatchers("/api/v1/users/**").hasRole("ADMIN")

                    // Device management
                    .pathMatchers("GET", "/api/v1/devices/**").hasAnyRole("VIEWER", "OPERATOR", "ADMIN")
                    .pathMatchers("POST", "/api/v1/devices/**").hasAnyRole("OPERATOR", "ADMIN")
                    .pathMatchers("PUT", "/api/v1/devices/**").hasAnyRole("OPERATOR", "ADMIN")
                    .pathMatchers("DELETE", "/api/v1/devices/**").hasRole("ADMIN")

                    // For test // todo delete later
                    .pathMatchers("/api/v1/testing/test").permitAll()
                    .pathMatchers("/api/v1/testing/secure-test").authenticated()

                    // Всё остальное - требует аутентификации
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(keycloakJwtConverter())
                }
            }
//            .csrf { it.disable() }
//            .oauth2Client(Customizer.withDefaults())
            .cors { it.configurationSource(corsConfig()) }
            .build()
    }

    private fun keycloakJwtConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<GrantedAuthority>()

            // get scopes
            val scopes = jwt.getClaimAsStringList("scope") ?: emptyList()
            scopes.forEach { scope ->
                authorities.add(SimpleGrantedAuthority("SCOPE_$scope"))
            }

            // get realm_access
            val realmAccess = jwt.getClaimAsMap("realm_access")
            if (realmAccess != null) {
                val roles = realmAccess["roles"]
                if (roles is List<*>) {
                    roles.filterIsInstance<String>().forEach { role ->
                        authorities.add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
                    }
                }
            }

            // get roles from resource_access
            val resourceAccess = jwt.getClaimAsMap("resource_access")
            resourceAccess?.forEach { (clientId, clientRoles) ->
                if (clientRoles is Map<*, *>) {
                    val roles = clientRoles["roles"]
                    if (roles is List<*>) {
                        roles.filterIsInstance<String>().forEach { role ->
                            authorities.add(
                                SimpleGrantedAuthority(
                                    "ROLE_${clientId.uppercase()}_${role.uppercase()}"
                                )
                            )
                        }
                    }
                }
            }
            authorities
        }
        return ReactiveJwtAuthenticationConverterAdapter(jwtConverter)
    }

    @Bean
    fun corsConfig(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }
}