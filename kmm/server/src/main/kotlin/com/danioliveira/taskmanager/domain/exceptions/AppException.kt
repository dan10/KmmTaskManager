package com.danioliveira.taskmanager.domain.exceptions

/**
 * Base exception class for all application-specific exceptions.
 * This class provides a common structure for all exceptions in the application.
 *
 * @property message A human-readable message describing the error
 * @property code A unique error code for this type of exception
 * @property status The HTTP status code to be returned to the client
 */
open class AppException(
    override val message: String,
    val code: String,
    val status: Int
) : RuntimeException(message)