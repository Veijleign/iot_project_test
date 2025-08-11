package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.UserRole
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRoleRepository :CoroutineCrudRepository<UserRole, UUID> {

    suspend fun findByUserIdAndRoleName(userId: UUID, roleName: String): UserRole?

}