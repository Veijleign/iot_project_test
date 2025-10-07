package org.iot_platform.userservice.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "user_roles")
class UserRole(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "role_name")
    var roleName: String, // admin, operator, viewer

    var scope: String? = null, // optional: specific scope like "building:123"

    @Column(name = "granted_at")
    var grantedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "granted_by")
    var grantedBy: UUID, // who issued a role

)
