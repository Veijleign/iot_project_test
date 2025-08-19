package org.iot_platform.userservice.utils

import org.iot_platform.userservice.config.exception.ExtendError
import org.iot_platform.userservice.config.exception.ExtendException
import java.util.Optional

fun <T> Optional<T>.getOrElseThrow(error: ExtendError, message: String): T {
    return this.orElseThrow { ExtendException.of(error, message) }
}

fun <T> Optional<T>.orElseThrow(exception: ExtendException): T {
    return this.orElseThrow { exception }
}

fun <T> T?.orThrow(error: ExtendError, message: String): T {
    return this ?: throw ExtendException.of(error, message)
}

fun <T> T?.orThrow(exception: ExtendException): T {
    return this ?: throw exception
}