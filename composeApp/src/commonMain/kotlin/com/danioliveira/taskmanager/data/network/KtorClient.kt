package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.data.storage.TokenStorage
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Platform-specific HTTP client engine factory
 */
expect fun createPlatformEngine(): HttpClientEngine

/**
 * Platform-specific base URL
 */
expect fun getBaseUrl(): String

/**
 * Ktor HTTP client for making API requests.
 */
class KtorClient(private val tokenStorage: TokenStorage) {

    // Create a JSON instance with lenient configuration
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    // Create the HTTP client
    fun generateClient() = HttpClient(createPlatformEngine()) {
        expectSuccess
        install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.getToken()?.let { BearerTokens(it, null) }
                }
            }
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }

        defaultRequest {
            url(getBaseUrl())
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    companion object {
        // Base URL is now platform-specific
    }
}
