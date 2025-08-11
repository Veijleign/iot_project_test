package org.iot_platform.userservice.payload.user

import java.util.*

data class UserRegistrationDto(
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val password: String,
    val organisationId: UUID? = null
)