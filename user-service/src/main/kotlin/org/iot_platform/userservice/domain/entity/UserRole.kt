package org.iot_platform.userservice.domain.entity

import jakarta.persistence.*
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_roles")
class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "user_id")
    var userId: UUID,

    var roleName: String, // admin, operator, viewer
    var scope: String? = null, // optional: specific scope like "building:123"

    @Column(name = "granted_at")
    var grantedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "granted_by")
    var grantedBy: UUID, // who issued a role

)
