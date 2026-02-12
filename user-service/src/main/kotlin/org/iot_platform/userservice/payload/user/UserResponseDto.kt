package org.iot_platform.userservice.payload.user

import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import java.time.LocalDateTime
import java.util.*

data class UserResponseDto(
    val id: UUID,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val status: UserStatus,
    val roles: List<String>,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)