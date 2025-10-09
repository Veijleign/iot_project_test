package org.iot_platform.userservice.service

import com.github.benmanes.caffeine.cache.Caffeine
import mu.KotlinLogging
import org.iot_platform.userservice.config.exception.AlreadyExistsException
import org.iot_platform.userservice.config.exception.KeycloakIntegrationException
import org.iot_platform.userservice.payload.keycloak.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class KeycloakService(
    private val restTemplate: RestTemplate,
    @Value("\${keycloak.server-url}") private val keycloakUrl: String,
    @Value("\${keycloak.main-realm}") private val realm: String,
    @Value("\${keycloak.admin-client-id}") private val adminClientId: String,
    @Value("\${keycloak.admin-client-secret}") private val adminClientSecret: String,
) {
    private val adminTokenCache = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.MINUTES)
        .build<String, String> { _ -> fetchAdminTokenBlocking() }

    fun createUser(userRegistrationDto: KeycloakUserCreationRequest): KeycloakUserResponse {
        val token = getCachedAdminToken()
        log.info("Creating user in Keycloak for username: ${userRegistrationDto.username}")
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }

        val request = HttpEntity(userRegistrationDto, headers)

        return try {
            log.info("Sending POST request to Keycloak for user creation.")
            val responseEntity = restTemplate.postForEntity(
                "$keycloakUrl/admin/realms/$realm/users",
                request,
                KeycloakUserResponse::class.java
            )
            log.info("Keycloak responded with status: ${responseEntity.statusCode}")

            if (responseEntity.statusCode.is2xxSuccessful) {
                val locationHeader = responseEntity.headers.location
                if (locationHeader != null) {
                    val userId = locationHeader.path.substringAfterLast("/")
                    log.info("Successfully created user in Keycloak with ID: $userId")
                    return KeycloakUserResponse(userId)
                } else {
                    log.error("Keycloak returned 2xx status but no Location header found. Response: $responseEntity")
                    throw KeycloakIntegrationException("Keycloak returned 2xx status but no Location header found.")
                }
            } else {
                log.error("Keycloak returned unexpected status: ${responseEntity.statusCode}")
                throw KeycloakIntegrationException("Keycloak returned unexpected status: ${responseEntity.statusCode}")
            }
        } catch (ex: HttpClientErrorException) {
            log.error("ERROR: ${ex.message}")
            log.error("Keycloak responded with status: ${ex.statusCode}")
            log.error("Response body (if any): ${ex.responseBodyAsString}")
            when (ex.statusCode) {
                HttpStatus.CONFLICT -> throw AlreadyExistsException("user already exists")
                HttpStatus.BAD_REQUEST -> throw KeycloakIntegrationException("invalid payload")
                else -> throw KeycloakIntegrationException("keycloak error: ${ex.statusCode}", ex)
            }
        } catch (ex: Exception) {
            log.error("Unexpected error during Keycloak user creation: ${ex.message}", ex)
            throw KeycloakIntegrationException("Failed to create user", ex)
        }
    }

    fun getUserById(keycloakUserId: String): KeycloakUserResponse {
        val token = getCachedAdminToken()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
        }

        val request = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId",
                HttpMethod.GET,
                request,
                KeycloakUserResponse::class.java
            )
            response.body ?: throw KeycloakIntegrationException("Empty Keycloak response for user $keycloakUserId")
        } catch (e: Exception) {
            log.error(e) { "Failed to get user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to get user $keycloakUserId", e)
        }
    }

    fun deleteUser(keycloakUserId: String) {
        val token = getCachedAdminToken()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
        }

        val request = HttpEntity<Void>(headers)

        try {
            restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId",
                HttpMethod.DELETE,
                request,
                Void::class.java
            )
            log.info { "Delete Keycloak user $keycloakUserId" }
        } catch (e: Exception) {
            log.error(e) { "CRITICAL: failed to delete keycloak user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to delete keycloak user $keycloakUserId", e)
        }
    }

    fun updateUser(keycloakUserId: String, userUpdate: KeycloakUserUpdateRequest) {
        val token = getCachedAdminToken()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }

        val request = HttpEntity(userUpdate, headers)

        try {
            restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId",
                HttpMethod.PUT,
                request,
                Void::class.java
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to update user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to update user $keycloakUserId", e)
        }
    }

    fun assignRoleToUser(keycloakUserId: String, roleName: String) {
        val token = getCachedAdminToken()
        val role = getRealmRole(roleName)
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }

        val request = HttpEntity(listOf(role), headers)

        try {
            restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm",
                HttpMethod.POST,
                request,
                Void::class.java
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to assign role to user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to assign role to user $keycloakUserId", e)
        }
    }

    fun removeRoleFromUser(keycloakUserId: String, roleName: String) {
        val token = getCachedAdminToken()
        val role = getRealmRole(roleName)
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
        }

        val request = HttpEntity(listOf(role), headers)

        try {
            restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm",
                HttpMethod.DELETE,
                request,
                Void::class.java
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to remove role to user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to remove role to user $keycloakUserId", e)
        }
    }

    fun getUserRoles(keycloakUserId: String): List<KeycloakRole> {
        val token = getCachedAdminToken()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
        }

        val request = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm",
                HttpMethod.GET,
                request,
                Array<KeycloakRole>::class.java
            )
            response.body?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getRealmRole(roleName: String): KeycloakRole {
        val token = getCachedAdminToken()
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $token")
        }

        val request = HttpEntity<Void>(headers)

        return try {
            val response = restTemplate.exchange(
                "$keycloakUrl/admin/realms/$realm/roles/$roleName",
                HttpMethod.GET,
                request,
                KeycloakRole::class.java
            )
            response.body ?: throw KeycloakIntegrationException("No role $roleName")
        } catch (e: Exception) {
            log.error(e) { "Failed to get realm role to user" }
            throw KeycloakIntegrationException("Failed to get realm role to user", e)
        }
    }

    private fun getCachedAdminToken(): String = adminTokenCache.get("admin-token")

    private fun fetchAdminTokenBlocking(): String {
        val tokenEndpoint = "$keycloakUrl/realms/$realm/protocol/openid-connect/token"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "client_credentials")
            add("client_id", adminClientId)
            add("client_secret", adminClientSecret)
        }

        val request = HttpEntity(body, headers)

        return try {
            val response = restTemplate.postForEntity(
                tokenEndpoint,
                request,
                TokenResponse::class.java
            )
            response.body?.accessToken ?: throw KeycloakIntegrationException("Empty token response")
        } catch (ex: Exception) {
            log.error(ex) { "Failed to fetch admin token" }
            throw KeycloakIntegrationException("Failed to fetch admin token", ex)
        }
    }
}