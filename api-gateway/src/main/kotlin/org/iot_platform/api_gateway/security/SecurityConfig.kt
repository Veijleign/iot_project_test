package org.iot_platform.api_gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
    private val reactiveOAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchange ->
                exchange
                    // публичные эндпоинты
                    .pathMatchers("/actuator/**", "api/v1/public/**").permitAll()

                    // Устройства - только для отправки телеметрии
                    .pathMatchers("/api/v1/telemetry/ingest")
                    .hasAnyAuthority("ROLE_iot-device", "SCOPE_device:telemetry")

                    // admin эндпоинты
                    .pathMatchers("/api/v1/devices/register").hasRole("ADMIN")
                    .pathMatchers("/api/v1/admin/**").permitAll()

                    // Device management
                    .pathMatchers("GET", "/api/v1/devices/**").hasAnyAuthority("ROLE_viewer", "SCOPE_device:read")
                    .pathMatchers(
                        "POST", "/api/v1/devices/**",
                        "PUT", "/api/v1/devices/**",
                        "DELETE", "/api/v1/devices/**"
                    )
                    .hasAnyAuthority("SCOPE_devices:write", "ROLE_operator", "ROLE_admin")

                    // User management - admins only
                    .pathMatchers("/api/v1/users/**").hasAuthority("ROLE_admin")

                    // For test // todo delete later
                    .pathMatchers("/api/v1/testing/test").permitAll()
                    .pathMatchers("/api/v1/testing/secure-test").authenticated()

                    // Всё остальное - требует аутентификации
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthConverter())
                }
            }
            .csrf { it.disable() }
            .oauth2Client(Customizer.withDefaults())
            .cors { it.configurationSource(corsConfig()) }
            .build()
    }

    private fun jwtAuthConverter(): ReactiveJwtAuthenticationConverterAdapter {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            val authorities = mutableListOf<SimpleGrantedAuthority>()

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
                        authorities.add(SimpleGrantedAuthority("ROLE_$role"))
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
                            authorities.add(SimpleGrantedAuthority("ROLE_${clientId}_$role"))
                        }
                    }
                }
            }

            println("JWT Claims: ${jwt.claims}")
            println("Extracted authorities: ${authorities.map { it.authority }}")
            authorities as Collection<GrantedAuthority>?
        }
        return ReactiveJwtAuthenticationConverterAdapter(jwtConverter)
    }

    @Bean
    fun authorizedClientManager(
        clients: ReactiveClientRegistrationRepository,
        auth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService
    ): ReactiveOAuth2AuthorizedClientManager {
        val provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()
        return AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, auth2AuthorizedClientService)
            .apply {
                setAuthorizedClientProvider(provider)
            }
    }

    @Bean
    fun corsConfig(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
            allowedHeaders = listOf("*")
            allowCredentials = false // при allowedOrigins = "*"
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }
}