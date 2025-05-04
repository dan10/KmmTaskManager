package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
    fun generateClient() = HttpClient {
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

        defaultRequest {
            url(BASE_URL)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    companion object {
        private const val BASE_URL = "http://localhost:8081"
    }
}
