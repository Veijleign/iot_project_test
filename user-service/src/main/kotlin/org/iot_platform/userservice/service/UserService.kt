package org.iot_platform.userservice.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.iot_platform.userservice.config.exception.AlreadyExistsException
import org.iot_platform.userservice.config.exception.ExtendError
import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.UserRole
import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.iot_platform.userservice.domain.repository.OrganizationRepository
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
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDateTime
import java.util.*

@Transactional
@Service
class UserService(
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val keycloakService: KeycloakService,
    // Внедряем значение из application.yml
    @Value("\${user.default-role}") private val defaultRole: String
) {

    suspend fun registerUser(registration: UserRegistrationDto): UserResponseDto {

        if (userRepository.findByUsername(registration.username) != null || userRepository.findByEmail(registration.email) != null)
            throw AlreadyExistsException("Username or email already exists")

        val keycloakRequest = KeycloakUserCreationRequest(
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

        val keycloakUser: KeycloakUserResponse
        try {
            keycloakUser = keycloakService.createUser(keycloakRequest)
        } catch (e: WebClientResponseException.Conflict) {
            throw AlreadyExistsException("Username or email already exists")
        }

        try {
            val user = userRepository.save(
                User(
                    keycloakUserId = keycloakUser.id,
                    username = registration.username,
                    email = registration.email,
                    firstName = registration.firstName,
                    lastName = registration.lastName,
                    organisationId = registration.organisationId,
                    status = UserStatus.ACTIVE,
                )
            )

            assignRole(user.id!!, defaultRole, null)

            return mapToResponseDto(user, listOf(defaultRole))
        } catch (e: Exception) {
            keycloakService.deleteUser(keycloakUser.id)
            throw RuntimeException("Failed to save user to the database")
        }
    }

    suspend fun getUserEntity(userId: UUID): User {
        return userRepository.findById(userId)
            .orThrow(
                ExtendError.NOT_FOUND_ERROR,
                "User with ID $userId does not exist"
            )
    }

    suspend fun getUserById(userId: UUID): UserResponseDto? {
        val user = getUserEntity(userId)

        val roles = userRoleRepository.findByUserId(userId).map { it.roleName }

        return mapToResponseDto(user, roles)
    }

    suspend fun getUserByKeycloakId(keycloakId: String): UserResponseDto? {
        val user = userRepository.findByKeycloakUserId(keycloakId)
            .orThrow(
                ExtendError.NOT_FOUND_ERROR,
                "User with ID $keycloakId does not exist"
            )

        val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }

        return mapToResponseDto(user, roles)
    }

    suspend fun updateUserProfile(userId: UUID, update: UserProfileUpdateDto): UserResponseDto? {
        val existingUser = userRepository.findById(userId) ?: return null
        val updatedUser = existingUser.copy(
            firstName = update.firstName ?: existingUser.firstName,
            lastName = update.lastName ?: existingUser.lastName,
            preferences = update.preferences?.let {
                ObjectMapper().writeValueAsString(it)
            } ?: existingUser.preferences,
            updatedAt = LocalDateTime.now(),
        )

        val saved = userRepository.save(updatedUser)

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

    suspend fun assignRole(
        userId: UUID,
        roleName: String,
        grantedBy: UUID?
    ): Boolean {
        val existing = userRoleRepository.findByUserIdAndRoleName(userId, roleName)
        if (existing != null) return false
        val userRole = UserRole(
            userId = userId,
            roleName = roleName,
            grantedBy = grantedBy ?: userId
        )

        userRoleRepository.save(userRole)

        // Синхронизируем с Keycloak
        val user = userRepository.findById(userId)
        if (user != null) {
            keycloakService.assignRoleToUser(
                user.keycloakUserId,
                roleName
            )
        }
        return true
    }

    suspend fun removeRole(userId: UUID, roleName: String): Boolean {
        val userRole = userRoleRepository.findByUserIdAndRoleName(userId, roleName) ?: return false

        userRoleRepository.delete(userRole)

        // keycloak sync
        val user = userRepository.findById(userId)
        if (user != null) {
            keycloakService.removeRoleFromUser(user.keycloakUserId, roleName)
        }

        return true
    }

    suspend fun getUsersByOrganizationResponse(organizationId: UUID): List<UserResponseDto> {
        val users = userRepository.findByOrganisationId(organizationId)

        return users.map { user ->
            val roles = userRoleRepository.findByUserId(user.id!!).map { it.roleName }
            mapToResponseDto(user, roles)
        }
    }

    suspend fun getAllUsersByOrganization(organizationId: UUID): List<User> =
        userRepository.findByOrganisationId(organizationId)

    suspend fun updateLastLogin(keycloakUserId: String) {
        val user = userRepository.findByKeycloakUserId(keycloakUserId)
        if (user != null) {
            val updated = user.copy(lastLoginAt = LocalDateTime.now())
            userRepository.save(updated)
        }
    }

    suspend fun deactivateUser(userId: UUID): Boolean {
        val user = userRepository.findById(userId) ?: return false

        val deactivated = user.copy(
            status = UserStatus.INACTIVE,
            updatedAt = LocalDateTime.now(),
        )

        userRepository.save(deactivated)

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
            organizationId = user.organisationId,
            status = user.status,
            roles = roles,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt,
        )
    }

}