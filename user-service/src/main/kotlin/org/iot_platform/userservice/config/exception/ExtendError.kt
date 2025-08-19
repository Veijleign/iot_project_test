package org.iot_platform.userservice.config.exception

import org.springframework.http.HttpStatus

enum class ExtendError(
    val internalCode: Long,
    val status: HttpStatus,
) {
    UNKNOWN_ERROR(-1L, HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_ERROR(1L, HttpStatus.NOT_FOUND),
    EXIST_ERROR(2L, HttpStatus.CONFLICT), // FOUND → CONFLICT для ясности
    BAD_REQUEST_ERROR(3L, HttpStatus.BAD_REQUEST),
    ACCESS_ERROR(4L, HttpStatus.FORBIDDEN),
    WRONG_ENTITY(5L, HttpStatus.BAD_REQUEST);

    companion object {
        fun fromInternalCode(code: Long): ExtendError? {
            return entries.find { it.internalCode == code }
        }
    }

}