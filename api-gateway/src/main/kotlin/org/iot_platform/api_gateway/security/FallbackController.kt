package org.iot_platform.api_gateway.security

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FallbackController {

    @GetMapping("/fallback/device-unavailable")
    fun deviceServiceUnavailable(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Device service is temporarily unavailable. Please try again later.")
    }

    @GetMapping("/fallback/user-unavailable")
    fun userServiceUnavailable(): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("User service is temporarily unavailable. Please try again later.")
    }

}