package org.iot_platform.userservice.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/testing")
class TestController {

    @GetMapping("/test")
    fun testEndpoint(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "User service is working!",
                "service" to "user-service",
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    @GetMapping("/secure-test")
    @PreAuthorize("hasRole('admin') or hasRole('operator')")
    fun secureTest(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "Secure endpoint accessed successfully",
                "user" to jwt.subject,
                "roles" to extractRoles(jwt),
                "scopes" to getScopesFromJwt(jwt)
            )
        )
    }

    private fun extractRoles(jwt: Jwt): List<String> {
        val roles = mutableListOf<String>()

        // Roles from realm_access
        val realmAccess = jwt.getClaimAsMap("realm_access")
        if (realmAccess != null) {
            val realmRoles = realmAccess["roles"]
            if (realmRoles is List<*>) {
                roles.addAll(realmRoles.filterIsInstance<String>())
            }
        }
        return roles
    }

    private fun getScopesFromJwt(jwt: Jwt): List<String> {
        return jwt.getClaimAsStringList("scope") ?: emptyList()
    }
}