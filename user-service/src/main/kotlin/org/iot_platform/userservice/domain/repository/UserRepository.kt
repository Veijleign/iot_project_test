package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {

    suspend fun findByKeycloakUserId(keycloakUserId: String): User?

    suspend fun findByUsername(username: String): User?

    suspend fun findByEmail(email: String): User?

    suspend fun findByOrganisationId(organisationId: UUID): List<User>


    @Query("""
        SELECT u FROM users u
        INNER JOIN user_roles ur ON u.id = ur.user_id
        WHERE ur.role_name = :roleName
    """)
    suspend fun findByRole(roleName: String): List<User>

    @Query("""
        SELECT COUNT(u) FROM users u
        WHERE u.organization_id = :organizationId
        AND u.status = 'ACTIVE'
    """)
    suspend fun countActiveUsersByOrganization(organisationId: UUID): Long

}