package org.iot_platform.userservice.config.exception

import mu.KotlinLogging

private val log = KotlinLogging.logger {}

open class ExtendException(
    val error: ExtendError,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    companion object {
        fun of(error: ExtendError, message: String): ExtendException {
            return ExtendException(error = error, message = message)
        }

        fun of(error: ExtendError, message: String, cause: Throwable): ExtendException {
            return ExtendException(error = error, message = message, cause = cause)
        }

        fun of(exception: Exception): ExtendException {
            if (exception is ExtendException) return exception

            log.error(exception) { "Unhandled exception converted to ExtendException" }

            return ExtendException(
                ExtendError.UNKNOWN_ERROR,
                "Internal Server Error",
            )
        }
    }
}

class NotFoundException(message: String) : ExtendException(ExtendError.NOT_FOUND_ERROR, message)
class AlreadyExistsException(message: String) : ExtendException(ExtendError.EXIST_ERROR, message)
class ValidationException(message: String) : ExtendException(ExtendError.BAD_REQUEST_ERROR, message)
class AccessDeniedException(message: String) : ExtendException(ExtendError.ACCESS_ERROR, message)
class KeycloakIntegrationException(message: String, cause: Throwable? = null) : ExtendException(ExtendError.UNKNOWN_ERROR, message, cause)