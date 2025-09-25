package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.data.tables.UsersTable
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Clock


/**
 * Generates a test JWT token for authentication in tests.
 *
 * @param userId The user ID to include in the token
 * @param email The email to include in the token
 * @return A JWT token string
 */
fun generateTestToken(userId: String, email: String): String {
    return JwtConfig.generateToken(userId, email)
}

/**
 * Extension function to set the Authorization header with a JWT token.
 *
 * @param token The JWT token to use for authentication
 */
fun HttpRequestBuilder.withAuth(token: String) {
    header(HttpHeaders.Authorization, "Bearer $token")
}

/**
 * Extension function to set the Content-Type header to application/json and
 * serialize the body object to JSON.
 *
 * @param body The object to serialize to JSON
 */
inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(Json.encodeToString(body))
}

/**
 * Creates a test user in the database.
 *
 * @param email The email of the user
 * @param passwordHash The password hash of the user
 * @param displayName The display name of the user
 * @param googleId The Google ID of the user (optional)
 * @return The ID of the created user
 */
suspend fun createTestUser(
    email: String = "test@example.com",
    passwordHash: String = "hashed-password",
    displayName: String = "Test User",
    googleId: String? = null
): String {
    return suspendTransaction {
        UsersTable.insertAndGetId { row ->
            row[UsersTable.email] = email
            row[UsersTable.passwordHash] = passwordHash
            row[UsersTable.displayName] = displayName
            row[UsersTable.googleId] = googleId
            row[UsersTable.createdAt] = Clock.System.now()
        }.value.toString()
    }
}
