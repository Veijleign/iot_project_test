package org.iot_platform.userservice.config.exception

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.*

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ExtendException::class)
    fun handleExtendException(ex: ExtendException): ResponseEntity<ExceptionBody> {
        return ResponseEntity
            .status(ex.error.status)
            .body(
                ExceptionBody(
                    internalCode = ex.error.internalCode,
                    status = ex.error.status,
                    errorMessage = ex.message ?: "No error message provided"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ExceptionBody> {
        val incidentId = UUID.randomUUID().toString()
//        log.error(ex) { "Unhandled exception, incidentId=$incidentId" }

        val safeMessage = "Unexpected server error. Incident id: $incidentId"

        val extendEx = ExtendException.of(ex)
        return ResponseEntity
            .status(extendEx.error.status)
            .body(
                ExceptionBody(
                    internalCode = extendEx.error.internalCode,
                    status = extendEx.error.status,
                    errorMessage = safeMessage
                )
            )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ExceptionBody> {
        return handleExtendException(ex)
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(ex: AlreadyExistsException): ResponseEntity<ExceptionBody> {
        return handleExtendException(ex)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ExceptionBody> {
        return handleExtendException(ex)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ExceptionBody> {
        return handleExtendException(ex)
    }

    @ExceptionHandler(KeycloakIntegrationException::class)
    fun handleKeycloakIntegrationException(ex: KeycloakIntegrationException): ResponseEntity<ExceptionBody> {
        return handleExtendException(ex)
    }

}