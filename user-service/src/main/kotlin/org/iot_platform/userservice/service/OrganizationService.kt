package org.iot_platform.userservice.service

import org.iot_platform.userservice.config.exception.NotFoundException
import org.iot_platform.userservice.domain.entity.Organization
import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.eKey.OrganizationStatus
import org.iot_platform.userservice.domain.mapper.OrganizationMapper
import org.iot_platform.userservice.domain.repository.OrganizationRepository
import org.iot_platform.userservice.payload.user.OrganizationDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class OrganizationService(
    private val userService: UserService,
    private val organizationRepository: OrganizationRepository,
    private val mapper: OrganizationMapper
) {
    fun createOrganization(dto: OrganizationDto): OrganizationDto {
        val org = organizationRepository.save(
            Organization(
                name = dto.name,
                description = dto.description,
                contactEmail = dto.contactEmail,
                status = dto.status ?: OrganizationStatus.ACTIVE
            )
        )

        return mapper.toDto(org)
    }

    fun deactivateOrganization(orgId: UUID): Organization? {
        val existing = getEntity(orgId)

        existing.status = OrganizationStatus.INACTIVE
        existing.updatedAt = LocalDateTime.now()

        return organizationRepository.save(existing)
    }

    fun getDtoById(orgId: UUID): OrganizationDto =
        mapper.toDto(getEntity(orgId))

    fun getEntity(orgId: UUID): Organization =
        organizationRepository.findById(orgId)
            .orElseThrow {
                NotFoundException("Organization with id $orgId not found")
            }

    fun getAllOrganizations(): List<Organization> =
        organizationRepository.findAll()

    fun getAllOrganizationDtos(): List<OrganizationDto> =
        mapper.toDtoList(organizationRepository.findAll())

    fun getAllActive(): List<Organization> =
        organizationRepository.findAllActive()

    fun getOrganizationUsers(orgId: UUID): List<User> =
        userService.getAllUsersByOrganization(orgId)

    fun isOrganizationActive(orgId: UUID): Boolean {
        val existing = getEntity(orgId)
        return existing.status == OrganizationStatus.ACTIVE
    }

}