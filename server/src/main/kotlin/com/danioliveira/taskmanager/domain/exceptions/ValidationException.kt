package com.danioliveira.taskmanager.domain.exceptions

import io.ktor.http.*

/**
 * Exception thrown when input validation fails.
 *
 * @property message A human-readable message describing the error
 * @property errors A map of field names to error messages
 */
class ValidationException(
    override val message: String = "Validation failed",
    val errors: Map<String, String> = emptyMap()
) : AppException(
    message = message,
    code = "VALIDATION_ERROR",
    status = HttpStatusCode.BadRequest.value
)