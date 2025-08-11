package org.iot_platform.userservice.payload.keycloak

data class KeycloakUserUpdateRequest(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val enabled: Boolean?
)