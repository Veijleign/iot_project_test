package org.iot_platform.userservice.service

import mu.KotlinLogging
import org.iot_platform.userservice.config.exception.NotFoundException
import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.UserRole
import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.iot_platform.userservice.domain.repository.UserRepository
import org.iot_platform.userservice.domain.repository.UserRoleRepository
import org.iot_platform.userservice.payload.keycloak.KeycloakUserResponse
import org.iot_platform.userservice.payload.user.UserRegistrationDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val log = KotlinLogging.logger {}

@Service
class UserDatabaseService(
    private val userRepository: UserRepository,
    private val userRoleRepository: UserRoleRepository,
    @Value("\${user.default-role}") private val defaultRole: String,
) {

    /* Только логика работы с БД */
    @Transactional
    fun saveUserProfileAndAssignRole(
        keycloakUser: KeycloakUserResponse,
        registration: UserRegistrationDto
    ): User {
        log.info {
            "INFO: $registration"
        }
        val user = userRepository.save(
            User(
                keycloakUserId = keycloakUser.id,
                username = registration.username,
                email = registration.email,
                firstName = registration.firstName,
                lastName = registration.lastName,
                status = UserStatus.ACTIVE,
            )
        )

        assignLocalRole(user.id!!, defaultRole, null)
        return user
    }

    fun assignLocalRole(
        userId: UUID,
        roleName: String,
        grantedBy: UUID?
    ): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            NotFoundException("User with ID $userId does not exist")
        }

        val existing = userRoleRepository.findByUserIdAndRoleName(userId, roleName)
        if (existing != null) return false
        val userRole = UserRole(
            user = user,
            roleName = roleName,
            grantedBy = grantedBy ?: userId
        )
        userRoleRepository.save(userRole)
        return true
    }


}