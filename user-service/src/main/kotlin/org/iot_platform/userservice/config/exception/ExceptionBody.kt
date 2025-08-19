package org.iot_platform.userservice.config.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import java.time.LocalDateTime

data class ExceptionBody(
    val internalCode: Long,
    val status: HttpStatus,
    val errorMessage: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
