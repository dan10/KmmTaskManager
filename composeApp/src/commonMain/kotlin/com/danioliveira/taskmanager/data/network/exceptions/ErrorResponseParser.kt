package com.danioliveira.taskmanager.data.network.exceptions

import co.touchlab.kermit.Logger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Utility class for parsing error responses from the server.
 */
object ErrorResponseParser {
    private val logger = Logger.withTag("ErrorResponseParser")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    /**
     * Parses the error response from the server.
     *
     * @param response The HTTP response to parse
     * @return A pair containing the error message and the raw error body
     */
    suspend fun parseErrorResponse(response: HttpResponse): Pair<String, String?> {
        return try {
            val errorBody = response.bodyAsText()
            val errorResponse = json.decodeFromString<ErrorResponse>(errorBody)

            // Use the error message from the server or fallback to status description
            val errorMessage = errorResponse.message
                ?: errorResponse.error
                ?: "Error ${response.status.value}: ${response.status.description}"

            errorMessage to errorBody
        } catch (e: Exception) {
            logger.w(e) { "Failed to parse error response: ${e.message}" }
            // Fallback to status description if parsing fails
            "Error ${response.status.value}: ${response.status.description}" to null
        }
    }

    /**
     * Data class representing the error response structure from the server.
     */
    @Serializable
    private data class ErrorResponse(
        @SerialName("message") val message: String? = null,
        @SerialName("error") val error: String? = null,
        @SerialName("status") val status: Int? = null,
        @SerialName("path") val path: String? = null
    )
}