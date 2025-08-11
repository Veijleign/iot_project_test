package org.iot_platform.userservice.payload.keycloak

data class KeycloakCredential(
    val type: String = "password",
    val value: String,
    val temporary: Boolean = false
)