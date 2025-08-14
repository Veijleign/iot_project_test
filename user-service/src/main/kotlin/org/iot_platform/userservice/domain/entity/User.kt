package org.iot_platform.userservice.domain.entity

import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("users")
data class User(
    @Id
    val id: UUID? = null,

    @Column("keycloak_user_id")
    val keycloakUserId: String, // id in Keycloak system

    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,

    @Column("organization_id")
    val organisationId: UUID?,
    val status: UserStatus = UserStatus.ACTIVE,
    val preferences: String? = null, // JSON with user settings

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column("last_login_at")
    val lastLoginAt: LocalDateTime? = null

)