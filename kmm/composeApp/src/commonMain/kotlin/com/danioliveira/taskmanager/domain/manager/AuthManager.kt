package com.danioliveira.taskmanager.domain.manager

import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for handling authentication state and navigation.
 * Responsible for checking if a user is logged in and managing authentication state.
 */
class AuthManager(
    private val tokenStorage: TokenStorage
) {
    // Authentication state
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /**
     * Checks if the user is authenticated by verifying if a token exists.
     * Updates the authentication state accordingly.
     *
     * @return True if the user is authenticated, false otherwise
     */
    suspend fun checkAuthState(): Boolean {
        val isAuthenticated = tokenStorage.getToken() != null
        _isAuthenticated.value = isAuthenticated
        return isAuthenticated
    }

    /**
     * Gets the current user information.
     *
     * @return The current user or null if not authenticated
     */
    suspend fun getCurrentUser(): User? {
        return tokenStorage.getUser()
    }

    /**
     * Logs out the user by clearing the token, user information, and updating the authentication state.
     */
    suspend fun logout() {
        tokenStorage.clearToken()
        tokenStorage.clearUser()
        _isAuthenticated.value = false
    }
}
