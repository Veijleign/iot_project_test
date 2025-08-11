package org.iot_platform.userservice.service

import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.UserRole
import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.iot_platform.userservice.domain.repository.OrganizationRepository
import org.iot_platform.userservice.domain.repository.UserRepository
import org.iot_platform.userservice.domain.repository.UserRoleRepository
import org.iot_platform.userservice.payload.keycloak.KeycloakCredential
import org.iot_platform.userservice.payload.keycloak.KeycloakUserCreationRequest
import org.iot_platform.userservice.payload.user.UserRegistrationDto
import org.iot_platform.userservice.payload.user.UserResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
@Service
class UserService(
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val keycloakService: KeycloakService
) {
    suspend fun registerUser(registration: UserRegistrationDto) : UserResponseDto {
        val keycloakUser = keycloakService.createUser(
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

        assignRole(user.id, "viewer", null)

    }

    suspend fun assignRole(
        userId: UUID,
        roleName: String,
        grantedBy: UUID?
    ) : Boolean {
        val existing= userRoleRepository.findByUserIdAndRoleName(userId, roleName)
        if (existing != null) return false
        val userRole = UserRole(
            userId = userId,
            roleName = roleName,
            grantedBy = grantedBy ?: userId
        )

        userRoleRepository.save(userRole)

        // Синхронизируем с Keycloak
        val user = userRepository.findById(userId)
        if(user != null) {
            keycloakService.assignRoleToUser(
                user.keycloakUserId,
                roleName
            )
        }
        return true
    }


}