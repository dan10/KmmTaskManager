package com.danioliveira.taskmanager.domain.exceptions

import io.ktor.http.*

/**
 * Exception thrown when a user is not authenticated or the authentication token is invalid.
 *
 * @property message A human-readable message describing the error
 */
class UnauthorizedException(
    override val message: String = "Authentication required"
) : AppException(
    message = message,
    code = "UNAUTHORIZED",
    status = HttpStatusCode.Unauthorized.value
)