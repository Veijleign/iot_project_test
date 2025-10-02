package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.Organization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OrganizationRepository : JpaRepository<Organization, UUID> {

    fun findByName(name: String): Organization?

    @Query("SELECT o  FROM Organization o WHERE o.status = 'ACTIVE'")
    fun findAllActive(): List<Organization>

}