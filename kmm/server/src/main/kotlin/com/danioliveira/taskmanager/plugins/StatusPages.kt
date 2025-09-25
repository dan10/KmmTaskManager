package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.domain.exceptions.AppException
import com.danioliveira.taskmanager.domain.exceptions.ErrorResponse
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

/**
 * Configures the StatusPages plugin to handle exceptions and return appropriate error responses.
 */
fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("ExceptionHandler")

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            val errors = cause.reasons.associateWith { it }
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    code = "VALIDATION_ERROR",
                    message = errors.map { it.key }.joinToString(", "),
                )
            )
        }

        // Handle AppException and its subclasses
        exception<AppException> { call, cause ->
            logger.error("AppException: ${cause.message}", cause)

            val response = ErrorResponse(
                status = cause.status,
                code = cause.code,
                message = cause.message
            )

            // Add validation errors to the response if available
            if (cause is ValidationException) {
                call.respond(
                    HttpStatusCode.fromValue(cause.status),
                    response.copy(details = cause.errors)
                )
            } else {
                call.respond(HttpStatusCode.fromValue(cause.status), response)
            }
        }

        // Map IllegalArgumentException to BadRequest for invalid inputs (e.g., malformed UUID)
        exception<IllegalArgumentException> { call, cause ->
            val response = ErrorResponse(
                status = HttpStatusCode.BadRequest.value,
                code = "INVALID_ARGUMENT",
                message = cause.message ?: "Invalid request"
            )
            call.respond(HttpStatusCode.BadRequest, response)
        }

        // Handle general exceptions
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ${cause.message}", cause)

            val response = ErrorResponse(
                status = HttpStatusCode.InternalServerError.value,
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred"
            )

            call.respond(HttpStatusCode.InternalServerError, response)
        }

        // Handle 404 Not Found for routes
        status(HttpStatusCode.NotFound) { call, _ ->
            val response = ErrorResponse(
                status = HttpStatusCode.NotFound.value,
                code = "ROUTE_NOT_FOUND",
                message = "The requested resource does not exist"
            )

            call.respond(HttpStatusCode.NotFound, response)
        }
    }
}