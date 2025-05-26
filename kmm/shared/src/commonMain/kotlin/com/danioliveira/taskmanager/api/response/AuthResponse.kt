package com.danioliveira.taskmanager.api.response

import com.danioliveira.taskmanager.domain.User
import kotlinx.serialization.Serializable

/**
 * Response model for authentication operations.
 * Contains the JWT token and the user information.
 */
@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)