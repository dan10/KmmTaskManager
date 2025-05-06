package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import com.danioliveira.taskmanager.data.network.AuthApiService
import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.repository.AuthRepository
import io.ktor.client.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Implementation of AuthRepository that uses AuthApiService and TokenStorage.
 *
 * @property apiService The API service for authentication operations
 * @property tokenStorage The storage for authentication tokens
 */
class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Login failed: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun register(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = withContext(Dispatchers.IO) { apiService.register(registerRequest) }
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Registration failed: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun saveToken(token: String) {
        tokenStorage.saveToken(token)
    }

    override suspend fun getToken(): String? {
        return tokenStorage.getToken()
    }

    override suspend fun clearToken() {
        tokenStorage.clearToken()
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenStorage.getToken() != null
    }
}
