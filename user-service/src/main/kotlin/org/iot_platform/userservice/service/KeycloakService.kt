package org.iot_platform.userservice.service

import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import mu.KotlinLogging
import org.iot_platform.userservice.config.exception.AlreadyExistsException
import org.iot_platform.userservice.config.exception.KeycloakIntegrationException
import org.iot_platform.userservice.payload.keycloak.KeycloakUserCreationRequest
import org.iot_platform.userservice.payload.keycloak.KeycloakUserUpdateRequest
import org.keycloak.admin.client.CreatedResponseUtil
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class KeycloakService(
    private val keycloak: Keycloak,
    @Value("\${keycloak.main-realm}") private val targetRealm: String
) {
    private fun getRealm(): RealmResource = keycloak.realm(targetRealm)

    fun createUser(dto: KeycloakUserCreationRequest): String {
        val user = UserRepresentation().apply {
            username = dto.username
            email = dto.email
            isEnabled = true
            isEmailVerified = true
        }

        var response: Response? = null
        try {
            response = getRealm().users().create(user)
        } catch (e: Exception) {
            throw KeycloakIntegrationException("Error calling Keycloak create", e)
        }

        if (response.status == 201) {
            val userId = CreatedResponseUtil.getCreatedId(response)

            try {
                val cred = CredentialRepresentation().apply {
                    type = CredentialRepresentation.PASSWORD
                    value = dto.password
                    isTemporary = false
                }
                getRealm().users().get(userId).resetPassword(cred)
                log.info { "Successfully created user $userId" }

                return userId
            } catch (e: Exception) {
                log.error("Failed to set password for user $userId", e)
                // откатить создание
                getRealm().users().get(userId).remove()
                throw KeycloakIntegrationException("User created but failed to set password", e)
            }
        } else if (response.status == 409) {
            throw AlreadyExistsException("User already exists: ${dto.username}")
        } else {
            val errorBody = response.readEntity(String::class.java)
            throw KeycloakIntegrationException("Keycloak returned status ${response.status}: $errorBody")
        }
        response.close()
    }

    fun getUserById(keycloakUserId: String): UserRepresentation {
        try {
            return getRealm().users().get(keycloakUserId).toRepresentation()
        } catch (e: NotFoundException) {
            throw KeycloakIntegrationException("not found: $keycloakUserId", e)
        } catch (e: Exception) {
            throw KeycloakIntegrationException("Failed to get user $keycloakUserId", e)
        }
    }

    fun deleteUser(userId: String) {
        try {
            getRealm().users().get(userId).remove()
            log.info("User removed $userId")
        } catch (ex: Exception) {
            log.error(ex) { "Failed to delete user $userId" }
            throw KeycloakIntegrationException("Failed to delete user $userId", ex)
        }
    }

    fun updateUser(keycloakUserId: String, updateDto: KeycloakUserUpdateRequest) {
        try {
            val user = getRealm().users().get(keycloakUserId).toRepresentation()

            user.firstName = updateDto.firstName
            user.lastName = updateDto.lastName
            user.email = updateDto.email

            // по идее это обновление
            getRealm().users().get(keycloakUserId).update(user)
            log.info { "Updated user $keycloakUserId" }
        } catch (e: Exception) {
            throw KeycloakIntegrationException("failed to update user $keycloakUserId", e)
        }
    }

    private fun getRoleRepresentation(roleName: String): RoleRepresentation {
        try {
            return getRealm().roles().get(roleName).toRepresentation()
        } catch (e: Exception) {
            throw KeycloakIntegrationException("failed to get role $roleName", e)
        }
    }

    fun assignRoleToUser(userId: String, roleName: String) {
        try {
            val role = getRoleRepresentation(roleName)

            getRealm().users().get(userId).roles().realmLevel().add(listOf(role))
            log.info { "Assigned role $roleName to user $userId" }
        } catch (e: Exception) {
            throw KeycloakIntegrationException("Failed to assign role to user $userId", e)
        }
    }

    fun removeRoleFromUser(userId: String, roleName: String) {
        try {
            val role = getRoleRepresentation(roleName)

            getRealm().users().get(userId).roles().realmLevel().remove(listOf(role))
            log.info { "Removed role $roleName from $userId" }
        } catch (e: Exception) {
            throw KeycloakIntegrationException("Failed to remove role from user $userId", e)
        }
    }

    fun getUserRoles(userId: String): List<RoleRepresentation> {
        return try {
            getRealm().users().get(userId).roles().realmLevel().listAll()
        } catch (e: Exception) {
            log.error("Failed to get roles for user $userId", e)
            emptyList()
        }
    }
}