package org.iot_platform.api_gateway.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/fallback")
class FallbackController {

    @GetMapping("/user")
    fun userServiceUnavailable(): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(mapOf(
                    "error" to "Service Unavailable",
                    "message" to "User Service is currently taking a nap. Please try again later."
                ))
        )
    }

    @GetMapping("/device")
    fun deviceServiceUnavailable(): Mono<ResponseEntity<Map<String, String>>> {
        return Mono.just(
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(mapOf(
                    "error" to "Service Unavailable",
                    "message" to "Device Service is overloaded. Please try again later."
                ))
        )
    }

}