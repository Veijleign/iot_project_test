package org.example.api_gateway

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean

private val log = KotlinLogging.logger {}

@SpringBootApplication
class ApiGatewayApplication {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        log.info {
            "Confuguring Custom routes for IoT Platform Gateway"
        }

        return builder.routes()
            .route("health-check") { route ->
                route.path("/health")
                    .uri("http://localhost:8090/actuator/health")
            }
            .route("api-docs") { route ->
                route.path("/api-docs/**")
                    .uri("no://op")
                    .filter { exchange, chain ->
                        chain.filter(exchange)

                    }

            }
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
