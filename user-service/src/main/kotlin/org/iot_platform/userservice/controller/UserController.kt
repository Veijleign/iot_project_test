package org.iot_platform.userservice.controller

import jakarta.validation.Valid
import mu.KotlinLogging
import org.iot_platform.userservice.domain.repository.OrganizationRepository
import org.iot_platform.userservice.payload.user.UserProfileUpdateDto
import org.iot_platform.userservice.payload.user.UserRegistrationDto
import org.iot_platform.userservice.payload.user.UserResponseDto
import org.iot_platform.userservice.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.util.*

private val log = KotlinLogging.logger {}


@RestController
@RequestMapping("/v1/users")
class UserController(
    private val userService: UserService,
    private val organizationService: OrganizationRepository,
) {
    @PostMapping("/register")
    fun registerUser(
        @Valid @RequestBody registrationDto: UserRegistrationDto
    ): ResponseEntity<UserResponseDto> {
        log.info("User registering request!")

        val user = userService.registerUser(registrationDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UserResponseDto> {
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
    fun updateCurrentUserProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody updateDto: UserProfileUpdateDto
    ): ResponseEntity<UserResponseDto> {
        val keycloakUserId = jwt.subject
        val user = userService.getUserByKeycloakId(keycloakUserId)
            ?: return ResponseEntity.notFound().build()

        val updated = userService.updateUserProfile(user.id, updateDto)

        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('admin') or hasRole('operator')")
    fun getUserById(@PathVariable userId: UUID): ResponseEntity<UserResponseDto> {
        val user = userService.getUserById(userId)

        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('admin')")
    fun assignRoleToUser(
        @PathVariable userId: UUID,
        @PathVariable roleName: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Map<String, String>> {
        val currentUser = userService.getUserByKeycloakId(jwt.subject) ?: return ResponseEntity.notFound().build()

        val success = userService.assignLocalRole(userId, roleName, currentUser.id)

        return if (success) {
            ResponseEntity.ok(
                mapOf(
                    "message" to "Role $roleName assigned to user successfully"
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to assign role or role already exists"
                )
            )
        }
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('admin')")
    fun removeRoleFromUser(
        @PathVariable userId: UUID,
        @PathVariable roleName: String,
    ): ResponseEntity<Map<String, String>> {
        val success = userService.removeRole(userId, roleName)

        return if (success) {
            ResponseEntity.ok(
                mapOf(
                    "message" to "Role $roleName removed from user successfully"
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to remove role from user"
                )
            )
        }
    }

    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('admin')")
    fun deactivateUser(
        @PathVariable userId: UUID,
    ): ResponseEntity<Map<String, String>> {
        val success = userService.deactivateUser(userId)

        return if (success) {
            ResponseEntity.ok(
                mapOf(
                    "message" to "User deactivated successfully"
                )
            )
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                    mapOf(
                        "error" to "Failed deactivate user"
                    )
                )
        }
    }

    @GetMapping("organization/{orgId}")
    @PreAuthorize("hasRole('admin') or hasRole('operator')")
    fun getUsersByOrganization(
        @PathVariable orgId: UUID
    ): ResponseEntity<List<UserResponseDto>> {
        val users = userService.getUsersByOrganizationResponse(orgId)
        return ResponseEntity.ok(users)
    }
}