package org.iot_platform.userservice.service

import kotlinx.coroutines.flow.Flow
import org.iot_platform.userservice.config.exception.NotFoundException
import org.iot_platform.userservice.domain.entity.Organization
import org.iot_platform.userservice.domain.entity.User
import org.iot_platform.userservice.domain.entity.eKey.OrganisationStatus
import org.iot_platform.userservice.domain.mapper.OrganizationMapper
import org.iot_platform.userservice.domain.repository.OrganizationRepository
import org.iot_platform.userservice.payload.user.OrganizationDto
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class OrganizationService(
    private val userService: UserService,
    private val organizationRepository: OrganizationRepository,
    private val mapper: OrganizationMapper
) {
    suspend fun createOrganization(dto: OrganizationDto): OrganizationDto {
        val org = organizationRepository.save(Organization(
            name = dto.name,
            description = dto.description,
            contactEmail = dto.contactEmail,
            status = dto.status ?: OrganisationStatus.ACTIVE
        ))

        return mapper.toDto(org)
    }

    suspend fun deactivateOrganization(orgId: UUID): Organization? {
        val existing = getEntity(orgId)
        val updated = existing.copy(
            status = OrganisationStatus.INACTIVE,
            updatedAt = LocalDateTime.now()
        )
        return organizationRepository.save(updated)
    }

    suspend fun getDtoById(orgId: UUID) : OrganizationDto =
        mapper.toDto(getEntity(orgId))

    suspend fun getEntity(orgId: UUID): Organization =
        organizationRepository.findById(orgId)
            ?: throw NotFoundException("Organization not found with id $orgId") // todo test

    suspend fun getAllOrganizations() : Flow<Organization> =
        organizationRepository.findAll()

    suspend fun getAllOrganizationDtos() : Flow<OrganizationDto> =
        mapper.toDtoList(organizationRepository.findAll())

    suspend fun getAllActive() : List<Organization> =
        organizationRepository.findAllActive()

    suspend fun getOrganizationUsers(orgId: UUID): List<User> = userService.getAllUsersByOrganization(orgId)

    suspend fun isOrganizationActive(orgId: UUID): Boolean {
        val existing = getEntity(orgId)
        return existing.status == OrganisationStatus.ACTIVE
    }

}