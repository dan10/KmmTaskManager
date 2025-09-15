package com.danioliveira.taskmanager.domain.exceptions

import io.ktor.http.*

/**
 * Exception thrown when an entity is already assigned/linked and the operation would duplicate it.
 */
class AlreadyAssignedException(
    override val message: String = "Resource is already assigned"
) : AppException(
    message = message,
    code = "ALREADY_ASSIGNED",
    status = HttpStatusCode.Conflict.value
)
