package org.iot_platform.userservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.iot_platform.userservice.config.exception.AlreadyExistsException
import org.iot_platform.userservice.config.exception.ExtendError
import org.iot_platform.userservice.config.exception.NotFoundException
import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.iot_platform.userservice.domain.repository.UserRepository
import org.iot_platform.userservice.domain.repository.UserRoleRepository
import org.iot_platform.userservice.payload.keycloak.KeycloakCredential
import org.iot_platform.userservice.payload.keycloak.KeycloakUserCreationRequest
import org.iot_platform.userservice.payload.keycloak.KeycloakUserResponse
import org.iot_platform.userservice.payload.keycloak.KeycloakUserUpdateRequest
import org.iot_platform.userservice.payload.user.UserProfileUpdateDto
import org.iot_platform.userservice.payload.user.UserRegistrationDto
import org.iot_platform.userservice.payload.user.UserResponseDto
import org.iot_platform.userservice.utils.orThrow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val keycloakService: KeycloakService,
    private val userDatabaseService: UserDatabaseService,
    @Value("\${user.default-role}") private val defaultRole: String,
) {

    fun registerUser(registration: UserRegistrationDto): UserResponseDto {
        if (userRepository.countUserByUsernameOrEmail(registration.username, registration.email) > 0) {
            log.error { "Username or email already exists" }
            throw AlreadyExistsException("Username or email already exists")
        }

        var keycloakUserId: String? = null
        try {
            val keycloakUser = registerUserInKeycloak(registration)
            keycloakUserId = keycloakUser.id

            val user = userDatabaseService.saveUserProfileAndAssignRole(keycloakUser, registration)

            keycloakService.assignRoleToUser(user.keycloakUserId, defaultRole)

            return mapToResponseDto(user, listOf(defaultRole))
        } catch (e: Exception) {
            // Компенсация если что-то не так
            if (keycloakUserId != null) {
                try {
                    keycloakService.deleteUser(keycloakUserId) // Use the captured ID
                    log.info("Compensating: Deleted user $keycloakUserId from Keycloak due to local DB failure.")
                } catch (deleteEx: Exception) {
                    log.error(
                        "Compensating: Failed to delete user $keycloakUserId from Keycloak after local DB failure: ${deleteEx.message}",
                        deleteEx
                    )
                }
            }
            log.error("Failed to register user due to: ${e.message}", e)
            throw RuntimeException("Failed to save user to the database or Keycloak interaction failed", e)
        }
    }

    private fun registerUserInKeycloak(registration: UserRegistrationDto): KeycloakUserResponse =
        keycloakService.createUser(
            KeycloakUserCreationRequest(
                registration.username,
                registration.email,
                registration.firstName,
                registration.lastName,
                credentials = listOf(
                    KeycloakCredential(
                        type = "password",
                        value = registration.password,
                        temporary = false
                    )
                )
            )
        )

    fun getUserEntity(userId: UUID): User =
        userRepository.findById(userId)
            .orElseThrow {
                NotFoundException("User with ID $userId does not exist")
            }

    fun getUserById(userId: UUID): UserResponseDto? {
        val user = getUserEntity(userId)

        val roles = userRoleRepository.findByUserId(userId).map { it.roleName }

        return mapToResponseDto(user, roles)
    }

    fun assignLocalRole(
        userId: UUID,
        roleName: String,
        grantedBy: UUID?
    ): Boolean = userDatabaseService.assignLocalRole(userId, roleName, grantedBy)

    fun getUserByKeycloakId(keycloakId: String): UserResponseDto? {
        val user = userRepository.findByKeycloakUserId(keycloakId)
            .orThrow(
                ExtendError.NOT_FOUND_ERROR,
                "User with ID $keycloakId does not exist"
            )

        val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }

        return mapToResponseDto(user, roles)
    }

    fun updateUserProfile(userId: UUID, update: UserProfileUpdateDto): UserResponseDto? {
        val existingUser = getUserEntity(userId)

        existingUser.firstName = update.firstName
        existingUser.lastName = update.lastName
        existingUser.preferences = ObjectMapper().writeValueAsString(update.preferences)
        existingUser.updatedAt = LocalDateTime.now()

        val saved = userRepository.save(existingUser)

        //keycloak update
        keycloakService.updateUser(
            existingUser.keycloakUserId,
            KeycloakUserUpdateRequest(
                firstName = update.firstName,
                lastName = update.lastName,
                email = null,
                enabled = null
            )
        )

        val roles = userRoleRepository.findByUserId(userId).map { it.roleName }
        return mapToResponseDto(saved, roles)
    }

    fun removeRole(userId: UUID, roleName: String): Boolean {
        val userRole = userRoleRepository.findByUserIdAndRoleName(userId, roleName) ?: return false

        userRoleRepository.delete(userRole)

        // keycloak sync
        val user = getUserEntity(userId)
        if (user != null) {
            keycloakService.removeRoleFromUser(
                user.keycloakUserId,
                roleName
            )
        }

        return true
    }

    fun getUsersByOrganizationResponse(organizationId: UUID): List<UserResponseDto> {
        val users = userRepository.findByOrganizationId(organizationId)

        return users.map { user ->
            val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }
            mapToResponseDto(user, roles)
        }
    }

    fun getAllUsersByOrganization(organizationId: UUID): List<User> =
        userRepository.findByOrganizationId(organizationId)

    fun updateLastLogin(keycloakUserId: String) {
        val user = userRepository.findByKeycloakUserId(keycloakUserId)
        if (user != null) {
            user.lastLoginAt = LocalDateTime.now()
            userRepository.save(user)
        }
    }

    fun deactivateUser(userId: UUID): Boolean {
        val user = getUserEntity(userId)

        user.status = UserStatus.INACTIVE
        user.updatedAt = LocalDateTime.now()

        userRepository.save(user)

        keycloakService.updateUser(
            user.keycloakUserId,
            KeycloakUserUpdateRequest(
                firstName = null,
                lastName = null,
                email = null,
                enabled = false
            )
        )
        return true
    }

    private fun mapToResponseDto(
        user: User,
        roles: List<String>,
    ): UserResponseDto {
        return UserResponseDto(
            id = user.id!!,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            organizationId = user.organization?.id,
            status = user.status,
            roles = roles,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }

}