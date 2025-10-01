package com.danioliveira.taskmanager.data.network

import co.touchlab.kermit.Logger
import com.danioliveira.taskmanager.data.network.exceptions.ClientErrorException
import com.danioliveira.taskmanager.data.network.exceptions.ErrorResponseParser
import com.danioliveira.taskmanager.data.network.exceptions.ForbiddenException
import com.danioliveira.taskmanager.data.network.exceptions.NotFoundException
import com.danioliveira.taskmanager.data.network.exceptions.ServerErrorException
import com.danioliveira.taskmanager.data.network.exceptions.UnauthorizedException
import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.manager.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
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
class KtorClient(
    private val tokenStorage: TokenStorage,
    private val authManager: AuthManager
) {

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
        expectSuccess = true

        configureTimeouts(this)
        configureAuthentication(this)
        configureContentNegotiation(this)
        configureResources(this)
        configureLogging(this)
        configureResponseValidation(this)
        configureDefaultRequest(this)
    }

    /**
     * Configures timeout settings for the HTTP client.
     */
    private fun configureTimeouts(config: HttpClientConfig<*>) {
        config.install(HttpTimeout) {
            // Set connection timeout to 30 seconds
            connectTimeoutMillis = 30.seconds.inWholeMilliseconds
            // Set request timeout to 30 seconds
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
            // Set socket timeout to 30 seconds
            socketTimeoutMillis = 30.seconds.inWholeMilliseconds
        }
    }

    /**
     * Configures authentication for the HTTP client.
     */
    private fun configureAuthentication(config: HttpClientConfig<*>) {
        config.install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.getToken()?.let { BearerTokens(it, null) }
                }
            }
        }
    }

    /**
     * Configures content negotiation for the HTTP client.
     */
    private fun configureContentNegotiation(config: HttpClientConfig<*>) {
        config.install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Configures resources support for the HTTP client.
     */
    private fun configureResources(config: HttpClientConfig<*>) {
        config.install(Resources)
    }

    /**
     * Configures logging for the HTTP client.
     */
    private fun configureLogging(config: HttpClientConfig<*>) {
        config.install(Logging) {
            level = LogLevel.ALL
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    // Use Kermit logger to log the message
                    this@KtorClient.logger.d { message }
                }
            }
        }
    }

    /**
     * Configures response validation for the HTTP client.
     */
    private fun configureResponseValidation(config: HttpClientConfig<*>) {
        config.HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                // Handle ClientRequestException (4xx errors)
                val clientException = exception as? ClientRequestException
                if (clientException != null) {
                    handleClientException(clientException, request)
                }

                // Handle ServerResponseException (5xx errors)
                val serverException = exception as? ServerResponseException
                if (serverException != null) {
                    handleServerException(serverException)
                }

                // Log and rethrow other exceptions
                logger.e(exception) { "Exception during request: ${request.url}" }
                throw exception
            }
        }
    }

    /**
     * Handles client exceptions (4xx errors).
     */
    private fun handleClientException(
        clientException: ClientRequestException,
        request: HttpRequest
    ) {
        val exceptionResponse = clientException.response
        val statusCode = exceptionResponse.status.value

        // Extract error message and body from response
        val (errorMessage, errorBody) = runBlocking {
            ErrorResponseParser.parseErrorResponse(exceptionResponse)
        }

        when (statusCode) {
            401 -> {
                logger.w { "Unauthorized: $errorMessage" }
                // Handle token expiration or invalid token by clearing it from storage
                runBlocking {
                    tokenStorage.clearToken()
                    authManager.logout()
                }
                throw UnauthorizedException(exceptionResponse, errorMessage, errorBody)
            }

            403 -> {
                logger.w { "Forbidden: $errorMessage" }
                throw ForbiddenException(exceptionResponse, errorMessage, errorBody)
            }

            404 -> {
                logger.w { "Page not found: ${request.url}" }
                throw NotFoundException(
                    exceptionResponse,
                    "Page not found: ${request.url}",
                    errorBody
                )
            }

            else -> {
                logger.w { "Client error: $errorMessage" }
                throw ClientErrorException(exceptionResponse, errorMessage, errorBody)
            }
        }
    }

    /**
     * Handles server exceptions (5xx errors).
     */
    private fun handleServerException(serverException: ServerResponseException) {
        val exceptionResponse = serverException.response
        val (errorMessage, errorBody) = runBlocking {
            ErrorResponseParser.parseErrorResponse(exceptionResponse)
        }

        logger.e { "Server error: $errorMessage" }
        throw ServerErrorException(exceptionResponse, errorMessage, errorBody)
    }

    /**
     * Configures default request settings for the HTTP client.
     */
    private fun configureDefaultRequest(config: HttpClientConfig<*>) {
        config.defaultRequest {
            url(getBaseUrl())
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
