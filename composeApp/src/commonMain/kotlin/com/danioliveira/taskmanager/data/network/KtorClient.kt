package com.danioliveira.taskmanager.data.network

import co.touchlab.kermit.Logger
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
import kotlin.time.Duration.Companion.seconds

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

    // Create a Kermit logger instance
    private val logger = Logger.withTag("KtorClient")

    // Create a JSON instance with lenient configuration
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    // Create the HTTP client
    fun generateClient() = HttpClient(createPlatformEngine()) {
        expectSuccess

        // Configure timeout settings
        install(HttpTimeout) {
            // Set connection timeout to 30 seconds
            connectTimeoutMillis = 30.seconds.inWholeMilliseconds
            // Set request timeout to 30 seconds
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
            // Set socket timeout to 30 seconds
            socketTimeoutMillis = 30.seconds.inWholeMilliseconds
        }

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
            level = LogLevel.ALL
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    // Use Kermit logger to log the message
                    this@KtorClient.logger.d { message }
                }
            }
        }

        defaultRequest {
            url(getBaseUrl())
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
