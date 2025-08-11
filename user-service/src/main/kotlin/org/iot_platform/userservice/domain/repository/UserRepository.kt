package org.iot_platform.userservice.domain.repository

import org.iot_platform.userservice.domain.entity.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CoroutineCrudRepository<User, UUID> {
}