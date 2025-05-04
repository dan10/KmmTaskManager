package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for authentication operations.
 */
class AuthApiService(
    private val client: HttpClient
) {

    /**
     * Authenticates a user with email and password.
     *
     * @param loginRequest The login request containing email and password
     * @return AuthResponse containing the JWT token and user information
     */
    suspend fun login(loginRequest: LoginRequest): AuthResponse {
        return client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }.body()
    }

    /**
     * Registers a new user with email, password, and display name.
     *
     * @param registerRequest The register request containing email, password, and display name
     * @return AuthResponse containing the JWT token and user information
     */
    suspend fun register(registerRequest: RegisterRequest): AuthResponse {
        return client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }.body()
    }
}
