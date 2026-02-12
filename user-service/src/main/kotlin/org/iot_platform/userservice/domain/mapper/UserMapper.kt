package org.iot_platform.userservice.domain.mapper

import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.payload.user.UserResponseDto
import org.springframework.stereotype.Component
import java.util.*

@Component
object UserMapper {

    fun toResponseDto(
        user: User,
        roles: List<String> = emptyList()
    ): UserResponseDto {
        return UserResponseDto(
            id = user.id ?: throw IllegalArgumentException("User ID cannot be null for response"),
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            status = user.status,
            roles = roles,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }

    fun toResponseDtoList(
        users: List<User>,
        rolesMap: Map<UUID, List<String>> = emptyMap()
    ): List<UserResponseDto> {
        return users.map { user ->
            toResponseDto(user, rolesMap[user.id] ?: emptyList())
        }
    }
}