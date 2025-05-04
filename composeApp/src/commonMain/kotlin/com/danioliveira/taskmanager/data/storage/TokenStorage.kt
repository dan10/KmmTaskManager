package com.danioliveira.taskmanager.data.storage

/**
 * Interface for token storage operations.
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
}