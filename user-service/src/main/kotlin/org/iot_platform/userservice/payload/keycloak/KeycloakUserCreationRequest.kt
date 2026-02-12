package org.iot_platform.userservice.payload.keycloak

data class KeycloakUserCreationRequest(
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val password: String,
    val enabled: Boolean = true,
    val emailVerified: Boolean = false
)