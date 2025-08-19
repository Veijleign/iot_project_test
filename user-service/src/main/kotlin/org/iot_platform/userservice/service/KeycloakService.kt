package org.iot_platform.userservice.service

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.iot_platform.userservice.payload.keycloak.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
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
        val token = getAdminToken()

        return webClient.post()
            .uri("$keycloakUrl/admin/realms/$realm/users")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userRegistrationDto)
            .retrieve()
            .bodyToMono(KeycloakUserResponse::class.java)
            .awaitSingle()
    }

    suspend fun deleteUser(keycloakUserId: String): Boolean {
        val token = getAdminToken()

        return try {
            webClient.delete()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .toBodilessEntity()
                .awaitSingle()
            log.info { "Deletey Keycloak user $keycloakUserId" }
            true
        } catch (e: Exception) {
            log.error(e) { "failed to delete keycloak user $keycloakUserId" }
            false
        }
    }

    suspend fun getUserById(keycloakUserId: String): KeycloakUserResponse? {
        val token = getAdminToken()

        return try {
            webClient.get()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .bodyToMono(KeycloakUserResponse::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(keycloakUserId: String, userUpdate: KeycloakUserUpdateRequest): Boolean {
        val token = getAdminToken()

        return try {
            webClient.put()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(userUpdate)
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun assignRoleToUser(keycloakUserId: String, roleName: String): Boolean {
        val token = getAdminToken()

        val role = getRealmRole(roleName) ?: return false

        return try {
            webClient.post()
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(listOf(role))
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeRoleFromUser(keycloakUserId: String, roleName: String): Boolean {
        val token = getAdminToken()
        val role = getRealmRole(roleName) ?: return false

        return try {
            webClient.method(HttpMethod.DELETE)
                .uri("$keycloakUrl/admin/realms/$realm/users/$keycloakUserId/role-mappings/realm")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .bodyValue(listOf(role))
                .retrieve()
                .bodyToMono(Void::class.java)
                .awaitSingle()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserRoles(keycloakUserId: String): List<KeycloakRole> {
        val token = getAdminToken()

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

    private suspend fun getRealmRole(roleName: String): KeycloakRole? {
        val token = getAdminToken()

        return try {
            webClient.get()
                .uri("$keycloakUrl/admin/realms/$realm/roles/$roleName")
                .header("Authorization", "Bearer $token")
                .retrieve()
                .bodyToMono(KeycloakRole::class.java)
                .awaitSingle()
        } catch (e: Exception) {
            null
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