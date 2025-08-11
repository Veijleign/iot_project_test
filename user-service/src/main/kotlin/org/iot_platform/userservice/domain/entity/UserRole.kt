package org.iot_platform.userservice.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Table("user_roles")
data class UserRole(
    @Id
    val id: UUID? = null,

    @Column("user_id")
    val userId: UUID,

    val roleName: String, // admin, operator, viewer
    val scope: String? = null, // optional: specific scope like "building:123"

    @Column("granted_at")
    val grantedAt: LocalDateTime = LocalDateTime.now(),

    @Column("granted_by")
    val grantedBy: UUID, // who issued a role

)
