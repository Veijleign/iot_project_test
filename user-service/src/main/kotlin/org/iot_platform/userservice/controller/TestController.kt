package org.iot_platform.userservice.controller

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class TestController {

    @GetMapping
    fun whoAmI(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mapOf(
                "principalClaimName" to jwt.principalClaimName,
                "authoritiesClaimName" to jwt.authoritiesClaimName
            ))
    }
}