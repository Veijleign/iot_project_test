package org.iot_platform.userservice.controller

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/users")
class TestController {

    @GetMapping("/me")
    fun whoAmI(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Map<String, Any?>> { // не очень хорошо что null может быть
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(mapOf(
                "sub" to jwt.subject,
                "referred_username" to (jwt.getClaimAsString("referred_username") ?: "unknown"),
                "email" to (jwt.getClaimAsString("email") ?: "unknown"),
                "roles" to getRolesFromJwt(jwt),
                "scopes" to getScopesFromJwt(jwt),
                "exp" to jwt.expiresAt?.epochSecond,
                "iat" to jwt.issuedAt?.epochSecond
            ))
    }

    @GetMapping("/test")
    fun testEndpoint() : ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "message" to "User service is working!",
            "service" to "user-service",
            "timestamp" to System.currentTimeMillis().toString()
        ))
    }

    private fun getRolesFromJwt(jwt: Jwt): List<String> {
        val roles = mutableListOf<String>()

        // Roles from realm_access
        val realmAccess = jwt.getClaimAsMap("realm_access")
        if (realmAccess != null) {
            val realmRoles = realmAccess["roles"]
            if(realmRoles is List<*>) {
                roles.addAll(realmRoles.filterIsInstance<String>())
            }
        }
        return roles
    }

    private fun getScopesFromJwt(jwt: Jwt) : List<String> {
        return jwt.getClaimAsStringList("scope") ?: emptyList()
    }

}