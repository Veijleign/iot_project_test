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
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    private val keycloakService: KeycloakService,
    private val userDatabaseService: UserDatabaseService,
    private val objectMapper: ObjectMapper,
    @Value("\${user.default-role}") private val defaultRole: String,
) {

    @Transactional
    fun registerUser(registration: UserRegistrationDto): UserResponseDto {
        if (userRepository.countUserByUsernameOrEmail(registration.username, registration.email) > 0) {
            log.warn { "Username or email already exists" }
            throw AlreadyExistsException("Username or email already exists")
        }

        var keycloakUserId: String? = null
        try {
            // create in keycloak
            val keycloakUserId = keycloakService.createUser(
                KeycloakUserCreationRequest(
                    username = registration.username,
                    email = registration.email,
                    firstName = registration.firstName,
                    lastName = registration.lastName,
                    password = registration.password,
                    emailVerified = true
                )
            )

            // save to local db
            val user = userDatabaseService.saveUserProfileAndAssignRole(
                keycloakUserId,
                registration
            )

            // assign role in Keycloak
            keycloakService.assignRoleToUser(keycloakUserId, defaultRole)

            return mapToResponseDto(user, listOf(defaultRole))
        } catch (e: Exception) {
            // Компенсация если что-то не так
            if (keycloakUserId != null) {
                try {
                    log.info("Compensating transaction: Deleting user $keycloakUserId from Keycloak")
                    keycloakService.deleteUser(keycloakUserId)
                } catch (deleteEx: Exception) {
                    log.error("Failed to rollback Keycloak user $keycloakUserId", deleteEx)
                }
            }
            log.error("Failed to register user", e)
            throw RuntimeException("Registration failed", e)
        }
    }

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

    fun getUserKeycloakId(keycloakId: String): UserResponseDto {
        val user = userRepository.findUserByKeycloakUserId(keycloakId)
            ?: throw NotFoundException("User with Keycloak ID $keycloakId not found")

        val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }
        return mapToResponseDto(user, roles)
    }

    fun getUserByKeycloakId(keycloakId: String): UserResponseDto? {
        val user = userRepository.findUserByKeycloakUserId(keycloakId)
            .orThrow(
                ExtendError.NOT_FOUND_ERROR,
                "User with ID $keycloakId does not exist"
            )

        val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }

        return mapToResponseDto(user, roles)
    }

    @Transactional
    fun updateUserProfile(
        userId: UUID,
        update: UserProfileUpdateDto
    ): UserResponseDto {
        val existingUser = getUserEntity(userId)

        existingUser.apply {
            firstName = update.firstName
            lastName = update.lastName
            preferences = ObjectMapper().writeValueAsString(update.preferences)
            updatedAt = LocalDateTime.now()
        }

        // local update
        val saved = userRepository.save(existingUser)

        // keycloak update
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

    @Transactional
    fun removeRole(
        userId: UUID,
        roleName: String
    ): Boolean {
        val userRole = userRoleRepository.findByUserIdAndRoleName(userId, roleName) ?: return false
        val user = getUserEntity(userId)

        // keycloak sync
        try {
            keycloakService.removeRoleFromUser(user.keycloakUserId, roleName)
        } catch (e: Exception) {
            log.error("Failed to remove role from keycloak: $roleName", e)
            throw e
        }

        userRoleRepository.delete(userRole)
        return true
    }

    @Transactional
    fun assignLocalRole(
        userId: UUID,
        roleName: String,
        grantedBy: UUID?
    ): Boolean {
        val assigned = userDatabaseService.assignLocalRole(userId, roleName, grantedBy)
        if (assigned) {
            val user = getUserEntity(userId)
            keycloakService.assignRoleToUser(user.keycloakUserId, roleName)
        }
        return assigned
    }

    @Transactional
    fun deactivateUser(userId: UUID): Boolean {
        val user = getUserEntity(userId)

        user.apply {
            status = UserStatus.INACTIVE
            updatedAt = LocalDateTime.now()
        }
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

    fun updateLastLogin(keycloakUserId: String) {
        val user = userRepository.findUserByKeycloakUserId(keycloakUserId)
        if (user != null) {
            user.lastLoginAt = LocalDateTime.now()
            userRepository.save(user)
        }
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
            status = user.status,
            roles = roles,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }

}