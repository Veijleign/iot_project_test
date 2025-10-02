package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

     fun findByKeycloakUserId(keycloakUserId: String): User?

     fun findByUsername(username: String): User?

     fun findByEmail(email: String): User?

     fun findByOrganisationId(organisationId: UUID): List<User>

    @Query("""
        SELECT * FROM users u
        INNER JOIN user_roles ur ON u.id = ur.user_id
        WHERE ur.role_name = :roleName
    """)
     fun findByRole(roleName: String): List<User>

    @Query("""
        SELECT COUNT(*) FROM users u
        WHERE u.organization_id = :organizationId
        AND u.status = 'ACTIVE'
    """)
     fun countActiveUsersByOrganization(organisationId: UUID): Long


     fun countUserByUsernameOrEmail(
        username: String,
        email: String
    ): Int
}