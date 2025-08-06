package org.iot_platform.api_gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
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
                    // admin эндпоинты
                    .pathMatchers("/api/v1/devices/register").hasRole("ADMIN")
                    // регистрация устройств только для DEVICE
                    .pathMatchers("/api/v1/telemetry/ingest").hasRole("DEVICE")
                    // сбор телеметрии (пример)
                    .pathMatchers("/api/v1/telemetry/ingest").hasAuthority("SCOPE_iot.write")
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
            // роли из realm_access
            val realmAccess = jwt.getClaimAsMap("realm_access")
            val roles = realmAccess?.get("roles") as List<String>
            roles.map { SimpleGrantedAuthority("ROLE_$it") }
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
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

}