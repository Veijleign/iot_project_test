package org.iot_platform.userservice.domain.entity

import org.iot_platform.userservice.domain.entity.eKey.OrganisationStatus
import org.iot_platform.userservice.domain.entity.eKey.UserStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table(name = "organization")
data class Organization (
    @Id
    val id: UUID? = null,

    val name: String?,
    val description: String,
    val contactEmail: String?,

    val status: OrganisationStatus = OrganisationStatus.ACTIVE,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)