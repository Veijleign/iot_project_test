package org.iot_platform.userservice.domain.entity

import jakarta.persistence.*
import org.iot_platform.userservice.domain.entity.eKey.OrganizationStatus
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "organization")
class Organization(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    var name: String?,
    var description: String?,

    @Column(name = "contact_email")
    var contactEmail: String?,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrganizationStatus,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    override fun equals(other: Any?) = when {
        this === other -> true
        other !is Organization -> false
        id == null || other.id == null -> false
        else -> id == other.id
    }

    override fun hashCode() = id?.hashCode() ?: 31
}