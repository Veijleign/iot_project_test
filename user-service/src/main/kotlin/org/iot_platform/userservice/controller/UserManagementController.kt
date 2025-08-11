package org.iot_platform.userservice.controller

import jakarta.validation.Valid
import org.iot_platform.userservice.domain.repository.OrganizationRepository
import org.iot_platform.userservice.payload.user.UserProfileUpdateDto
import org.iot_platform.userservice.payload.user.UserRegistrationDto
import org.iot_platform.userservice.payload.user.UserResponseDto
import org.iot_platform.userservice.service.UserService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/users")
class UserManagementController(
    private val userService: UserService,
    private val organizationRepository: OrganizationRepository,
) {

    @PostMapping("/register")
    suspend fun registerUser(
        @Valid @RequestBody registrationDto: UserRegistrationDto
    ) : ResponseEntity<UserResponseDto> {
        return try {
            val user = userService.registerUser(registrationDto)
            ResponseEntity.status(HttpStatus.CREATED).body(user)

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(null)
        }
    }

    @GetMapping("/me")
    suspend fun getCurrentUser(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserResponseDto> { // не очень хорошо что null может быть
        val keycloakUserId = jwt.subject

        // update last time log in
        userService.updateLastLogin(keycloakUserId)

        val user = userService.getUserByKeycloakId(keycloakUserId)

        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/me")
    suspend fun updateCurrentUserProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody updateDto: UserProfileUpdateDto
    ) : ResponseEntity<UserResponseDto> {
        val keycloakUserId = jwt.subject
        val user = userService.getUserByKeycloakId(keycloakUserId) ?: return ResponseEntity.notFound().build()

        val updated = userService.updateUserProfile(user.id, updateDto)

        return if(updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.badRequest().build()
        }
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