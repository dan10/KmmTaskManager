package com.danioliveira.taskmanager.domain.usecase.register

import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import com.danioliveira.taskmanager.domain.repository.AuthRepository

/**
 * Use case for user registration.
 *
 * @property authRepository The repository for authentication operations
 */
class RegisterUseCase(private val authRepository: AuthRepository) {

    /**
     * Executes the registration use case.
     *
     * @param email The user's email
     * @param password The user's password
     * @param displayName The user's display name
     * @return Result containing AuthResponse on success or an exception on failure
     */
    suspend operator fun invoke(email: String, password: String, displayName: String): Result<AuthResponse> {
        val registerRequest = RegisterRequest(email = email, password = password, displayName = displayName)
        val result = authRepository.register(registerRequest)

        result.onSuccess { authResponse ->
            // Save the token for future requests
            authRepository.saveToken(authResponse.token)
        }

        return result
    }
}