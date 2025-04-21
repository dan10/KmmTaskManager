package com.danioliveira.taskmanager.domain.exceptions

import kotlinx.serialization.Serializable

/**
 * A standardized error response format for API errors.
 *
 * @property status The HTTP status code
 * @property code A unique error code
 * @property message A human-readable error message
 * @property details Additional error details (optional)
 */
@Serializable
data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)