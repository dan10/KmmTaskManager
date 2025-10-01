package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import com.danioliveira.taskmanager.api.routes.Auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

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
        return client.post(Auth.Login()) {
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
        return client.post(Auth.Register()) {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }.body()
    }
}
