package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.response.AuthResponse

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    /**
     * Authenticates a user with email and password.
     *
     * @param loginRequest The login request containing email and password
     * @return AuthResponse containing the JWT token and user information
     */
    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse>

    /**
     * Saves the authentication token for future requests.
     *
     * @param token The JWT token to save
     */
    suspend fun saveToken(token: String)

    /**
     * Gets the saved authentication token.
     *
     * @return The saved JWT token or null if not found
     */
    suspend fun getToken(): String?

    /**
     * Clears the saved authentication token.
     */
    suspend fun clearToken()

    /**
     * Checks if the user is authenticated.
     *
     * @return True if the user is authenticated, false otherwise
     */
    suspend fun isAuthenticated(): Boolean
}