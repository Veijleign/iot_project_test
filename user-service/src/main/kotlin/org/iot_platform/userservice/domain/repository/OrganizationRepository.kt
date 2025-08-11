package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.Organization
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrganizationRepository : CoroutineCrudRepository<Organization, UUID> {
}