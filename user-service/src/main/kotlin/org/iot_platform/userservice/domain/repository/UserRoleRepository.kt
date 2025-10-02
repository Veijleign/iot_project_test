package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRoleRepository : JpaRepository<UserRole, UUID> {

    fun findByUserId(userId: UUID): List<UserRole>

    fun findByUserIdAndRoleName(userId: UUID, roleName: String): UserRole?

    fun findByRoleName(roleName: String): List<UserRole>

    fun deleteByUserIdAndRoleName(userId: UUID, roleName: String)

    @Query(
        """
        SELECT ur FROM user_roles ur
        INNER JOIN users u On ur.user_id = u.id
        WHERE u.organization_id = :organizationId
        AND ur.role_name = :roleName
    """
    )
    fun findByOrganizationAndRole(organization: UUID, roleName: String): List<UserRole>

}