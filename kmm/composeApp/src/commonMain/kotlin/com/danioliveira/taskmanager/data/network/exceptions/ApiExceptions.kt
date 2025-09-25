package com.danioliveira.taskmanager.data.network.exceptions

import io.ktor.client.statement.HttpResponse

/**
 * Base exception class for API-related errors.
 *
 * @property response The HTTP response that caused the exception
 * @property message A human-readable error message
 * @property errorBody The raw error body from the server, if available
 */
open class ApiException(
    val response: HttpResponse,
    override val message: String,
    val errorBody: String? = null
) : Exception(message)

/**
 * Exception thrown when a server error (5xx) occurs.
 */
class ServerErrorException(
    response: HttpResponse,
    message: String,
    errorBody: String? = null
) : ApiException(response, message, errorBody)

/**
 * Exception thrown when authentication fails (401 Unauthorized).
 */
class UnauthorizedException(
    response: HttpResponse,
    message: String,
    errorBody: String? = null
) : ApiException(response, message, errorBody)

/**
 * Exception thrown when access is forbidden (403 Forbidden).
 */
class ForbiddenException(
    response: HttpResponse,
    message: String,
    errorBody: String? = null
) : ApiException(response, message, errorBody)

/**
 * Exception thrown when a resource is not found (404 Not Found).
 */
class NotFoundException(
    response: HttpResponse,
    message: String,
    errorBody: String? = null
) : ApiException(response, message, errorBody)

/**
 * Exception thrown for other client errors (4xx).
 */
class ClientErrorException(
    response: HttpResponse,
    message: String,
    errorBody: String? = null
) : ApiException(response, message, errorBody)