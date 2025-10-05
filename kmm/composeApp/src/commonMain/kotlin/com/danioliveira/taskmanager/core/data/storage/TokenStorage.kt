package com.danioliveira.taskmanager.data.storage

import com.danioliveira.taskmanager.domain.User

/**
 * Interface for token and user storage operations.
 */
interface TokenStorage {
    /**
     * Saves the authentication token.
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
     * Saves the current user information.
     *
     * @param user The user information to save
     */
    suspend fun saveUser(user: User)

    /**
     * Gets the saved user information.
     *
     * @return The saved user information or null if not found
     */
    suspend fun getUser(): User?

    /**
     * Clears the saved user information.
     */
    suspend fun clearUser()
}