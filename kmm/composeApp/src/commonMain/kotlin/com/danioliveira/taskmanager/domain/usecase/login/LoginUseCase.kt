package com.danioliveira.taskmanager.domain.usecase.login

import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import com.danioliveira.taskmanager.domain.repository.AuthRepository

/**
 * Use case for user login.
 *
 * @property authRepository The repository for authentication operations
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    /**
     * Executes the login use case.
     *
     * @param email The user's email
     * @param password The user's password
     * @return Result containing AuthResponse on success or an exception on failure
     */
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        val loginRequest = LoginRequest(email = email, password = password)
        val result = authRepository.login(loginRequest)

        result.onSuccess { authResponse ->
            // Save the token and user information for future requests
            authRepository.saveToken(authResponse.token)
            authRepository.saveUser(authResponse.user)
        }

        return result
    }
}