package org.iot_platform.userservice.payload.keycloak

data class KeycloakUserResponse(
    val id: String,
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val enabled: Boolean,
    val emailVerified: Boolean,
    val createdTimestamp: Long?
)