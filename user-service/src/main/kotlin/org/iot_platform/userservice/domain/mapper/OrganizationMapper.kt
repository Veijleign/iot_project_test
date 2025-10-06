package org.iot_platform.userservice.domain.mapper

import org.iot_platform.userservice.domain.entity.Organization
import org.iot_platform.userservice.payload.user.OrganizationDto
import org.springframework.stereotype.Component

@Component
object OrganizationMapper {

    fun toDto(organization: Organization): OrganizationDto {
        return OrganizationDto(
            id = organization.id,
            name = organization.name ?: "",
            description = organization.description,
            contactEmail = organization.contactEmail,
            status = organization.status
        )
    }

    fun toDtoList(organizations: List<Organization>): List<OrganizationDto> {
        return organizations.map { toDto(it) }
    }

}