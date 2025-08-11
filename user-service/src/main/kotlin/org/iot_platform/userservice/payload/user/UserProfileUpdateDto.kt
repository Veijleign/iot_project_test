package org.iot_platform.userservice.payload.user

data class UserProfileUpdateDto(
    val firstName: String?,
    val lastName: String?,
    val preferences: Map<String, Any>?
)