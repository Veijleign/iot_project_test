package org.iot_platform.userservice.config.exception

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
            return if (exception is ExtendException) {
                exception
            } else {
                ExtendException(ExtendError.UNKNOWN_ERROR, exception.message ?: "UnknownError", exception)
            }
        }
    }
}

class NotFoundException(message: String) : ExtendException(ExtendError.NOT_FOUND_ERROR, message)
class AlreadyExistsException(message: String) : ExtendException(ExtendError.EXIST_ERROR, message)
class ValidationException(message: String) : ExtendException(ExtendError.BAD_REQUEST_ERROR, message)
class AccessDeniedException(message: String) : ExtendException(ExtendError.ACCESS_ERROR, message)