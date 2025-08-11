package org.iot_platform.userservice.payload.keycloak

data class KeycloakRole(
    val id: String,
    val name: String,
    val description: String?
)