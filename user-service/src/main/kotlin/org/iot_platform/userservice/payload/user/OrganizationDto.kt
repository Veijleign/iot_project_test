package org.iot_platform.userservice.payload.user

import org.iot_platform.userservice.domain.entity.eKey.OrganisationStatus
import java.util.*

data class OrganizationDto(
    val id: UUID?,
    val name: String,
    val description: String?,
    val contactEmail: String?,
    val status: OrganisationStatus?
)