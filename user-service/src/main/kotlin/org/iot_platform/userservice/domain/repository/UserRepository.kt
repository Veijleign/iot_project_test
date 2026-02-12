package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    @Query(
        """
        SELECT u FROM User u
        INNER JOIN UserRole ur ON u.id = ur.id
        WHERE ur.roleName = :roleName
    """
    )
    fun findByRole(roleName: String): List<User>

    fun findUserByKeycloakUserId(keycloakId: String): User?

    fun countUserByUsernameOrEmail(
        username: String,
        email: String
    ): Int
}