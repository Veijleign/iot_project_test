package org.iot_platform.api_gateway.security

import org.springframework.boot.availability.ApplicationAvailabilityBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController(private val applicationAvailability: ApplicationAvailabilityBean) {

    @GetMapping("/healthcheck")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mapOf(
                "status" to "UP",
                "liveness" to applicationAvailability.livenessState.name,
                "readiness" to applicationAvailability.readinessState.name,
                ))
    }
}