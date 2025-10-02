package org.iot_platform.userservice.service

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.iot_platform.userservice.config.exception.AlreadyExistsException
import org.iot_platform.userservice.config.exception.KeycloakIntegrationException
import org.iot_platform.userservice.payload.keycloak.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class KeycloakService(
    private val webClient: WebClient,
    @Value("\${keycloak.server-url}") private val keycloakUrl: String,
    @Value("\${keycloak.main-realm") private val realm: String,
    @Value("\${keycloak.admin-client-id}") private val adminClientId: String,
    @Value("\${keycloak.admin-client-secret}") private val adminClientSecret: String,
) {
    private val adminTokenCache: AsyncLoadingCache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.MINUTES)
        .buildAsync { _, _ ->
            getAdminToken().toFuture()
        }

    suspend fun createUser(userRegistrationDto: KeycloakUserCreationRequest): KeycloakUserResponse {
        val token = getCachedAdminToken()
        try {
            return webClient.post()
                .uri("$keycloakUrl/admin/realms/$realm/users")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRegistrationDto)
                .exchangeToMono { resp ->
                    when {
                        resp.statusCode().is2xxSuccessful -> {
                            // try read body; if empty, extract Location header and GET user by id
                            resp.bodyToMono(KeycloakUserResponse::class.java)
                                .switchIfEmpty(
                                    Mono.defer {
                                        val location = resp.headers().asHttpHeaders().location
                                        if (location != null) {
                                            // extract user id from last path segment
                                            val id = location.path.substringAfterLast("/")
                                            getUserByIdReactive(id, token) // returns KeycloakUserResponse
                                        } else {
                                            Mono.error(KeycloakIntegrationException("No body and no Location header on create user"))
                                        }
                                    }
                                )
                        }
                        resp.statusCode() == HttpStatus.CONFLICT -> Mono.error(
                            WebClientResponseException.create(
                                resp.statusCode().value(),
                                "Conflict",
                                resp.headers().asHttpHeaders(),
                                ByteArray(0),
                                null
                            )
                        )
                        else -> resp.createException().flatMap { Mono.error(it) }
                    }
                }
                .awaitSingle()
        } catch (ex: WebClientResponseException) {
            when (ex.statusCode) {
                HttpStatus.CONFLICT -> throw AlreadyExistsException("user already exists")
                HttpStatus.BAD_REQUEST -> throw KeycloakIntegrationException("invalid payload")
                else -> throw KeycloakIntegrationException("keycloak error: ${ex.statusCode}", ex)
            }
        }
    }

    suspend fun getUserById(keycloakUserId: String): KeycloakUserResponse {
        val token = getCachedAdminToken()

        return try {
            webClient.get()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .bodyToMono(KeycloakUserResponse::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            log.error(e) { "Failed to get user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to get user $keycloakUserId", e)
        }
    }

    suspend fun deleteUser(keycloakUserId: String) {
        val token = getCachedAdminToken()
        return try {
            webClient.delete()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .toBodilessEntity()
                .awaitSingle()
            log.info { "Delete Keycloak user $keycloakUserId" }
        } catch (e: Exception) {
            log.error(e) { "CRITICAL: failed to delete keycloak user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to delete keycloak user $keycloakUserId", e)
        }
    }

    suspend fun updateUser(keycloakUserId: String, userUpdate: KeycloakUserUpdateRequest) {
        val token = getCachedAdminToken()

        try {
            webClient.put()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(userUpdate)
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            log.error(e) { "Failed to update user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to update user $keycloakUserId", e)
        }
    }

    suspend fun assignRoleToUser(keycloakUserId: String, roleName: String) {
        val token = getCachedAdminToken()
        val role = getRealmRole(roleName)

        try {
            webClient.post()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(listOf(role))
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            log.error(e) { "Failed to assign role to user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to assign role to user $keycloakUserId", e)
        }
    }

    suspend fun removeRoleFromUser(keycloakUserId: String, roleName: String) {
        val token = getCachedAdminToken()
        val role = getRealmRole(roleName)

        try {
            webClient.method(HttpMethod.DELETE)
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(listOf(role))
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            log.error(e) { "Failed to remove role to user $keycloakUserId" }
            throw KeycloakIntegrationException("Failed to remove role to user $keycloakUserId", e)
        }
    }

    suspend fun getUserRoles(keycloakUserId: String): List<KeycloakRole> {
        val token = getCachedAdminToken()

        return try {
            webClient.get()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .bodyToFlux(KeycloakRole::class.java)
                .collectList()
                .awaitSingle()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getUserByIdReactive(keycloakUserId: String, token: String): Mono<KeycloakUserResponse> =
        webClient.get()
            .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono(KeycloakUserResponse::class.java)


    private suspend fun getRealmRole(roleName: String): KeycloakRole {
        val token = getCachedAdminToken()

        return try {
            webClient.get()
                .uri("$keycloakUrl/admin/realms/$realm/roles/$roleName")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .bodyToMono(KeycloakRole::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            log.error(e) { "Failed to get realm role to user" }
            throw KeycloakIntegrationException("Failed to get realm role to user", e)
        }
    }

    private suspend fun getCachedAdminToken(): String {
        return adminTokenCache.get("admin-token").await()
    }

    private fun getAdminToken(): Mono<String> {
        val tokenEndpoint = "$keycloakUrl/realms/$realm/protocol/openid-connect/token"

        return webClient
            .post()
            .uri(tokenEndpoint)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "client_credentials")
                    .with("client_id", adminClientId)
                    .with("client_secret", adminClientSecret)
            )
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .map { it.accessToken }
    }
}