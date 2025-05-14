package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.auth.TestGoogleTokenVerifier
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest : KoinTest {

    @BeforeTest
    fun setUp() {
        // Clear the database before each test
        TestDatabase.init()
    }

    @AfterTest
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()
        stopKoin()
    }

    @Test
    fun `test user registration - successful`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Prepare registration request
        val registerRequest = RegisterRequest(
            email = "newuser@example.com",
            password = "password123",
            displayName = "New User"
        )

        val response = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(registerRequest))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("token"))
        assertTrue(responseBody.contains("New User"))
    }

    @Test
    fun `test user registration - email already registered`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // First, register a user
        val firstRegisterRequest = RegisterRequest(
            email = "existinguser@example.com",
            password = "password123",
            displayName = "Existing User"
        )

        val firstResponse = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(firstRegisterRequest))
        }

        // Verify the first registration was successful
        assertEquals(HttpStatusCode.OK, firstResponse.status)

        // Now try to register another user with the same email
        val secondRegisterRequest = RegisterRequest(
            email = "existinguser@example.com",
            password = "anotherpassword",
            displayName = "Another User"
        )

        val secondResponse = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(secondRegisterRequest))
        }

        // Verify the second registration failed with a Bad Request status
        assertEquals(HttpStatusCode.BadRequest, secondResponse.status)

        // Verify the response contains an error message about the email
        val responseBody = secondResponse.bodyAsText()
        assertTrue(responseBody.contains("email"))
        assertTrue(responseBody.contains("already registered"))
    }

    @Test
    fun `test user login - successful`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // First, register a user
        val registerRequest = RegisterRequest(
            email = "logintest@example.com",
            password = "password123",
            displayName = "Login Test User"
        )

        val registerResponse = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(registerRequest))
        }

        // Verify the registration was successful
        assertEquals(HttpStatusCode.OK, registerResponse.status)

        // Now try to login with the registered user
        val loginRequest = LoginRequest(
            email = "logintest@example.com",
            password = "password123"
        )

        val loginResponse = client.post("api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginRequest))
        }

        // Verify the login was successful
        assertEquals(HttpStatusCode.OK, loginResponse.status)

        // Verify the response contains a token and user information
        val responseBody = loginResponse.bodyAsText()
        assertTrue(responseBody.contains("token"))
        assertTrue(responseBody.contains("Login Test User"))
    }

    @Test
    fun `test user login - invalid credentials`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // First, register a user
        val registerRequest = RegisterRequest(
            email = "invalidlogin@example.com",
            password = "password123",
            displayName = "Invalid Login Test User"
        )

        val registerResponse = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(registerRequest))
        }

        // Verify the registration was successful
        assertEquals(HttpStatusCode.OK, registerResponse.status)

        // Now try to login with incorrect password
        val loginRequest = LoginRequest(
            email = "invalidlogin@example.com",
            password = "wrongpassword"
        )

        val loginResponse = client.post("api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(loginRequest))
        }

        // Verify the login failed with Unauthorized status
        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)

        // Verify the response contains an error message
        val responseBody = loginResponse.bodyAsText()
        assertTrue(responseBody.contains("Invalid email or password"))
    }

    @Test
    fun `test user registration - invalid email format`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Try to register with an invalid email format
        val registerRequest = RegisterRequest(
            email = "invalidemail",  // Invalid email format
            password = "password123",
            displayName = "Invalid Email User"
        )

        // This should throw a RequestValidationException which is converted to a ValidationException
        // The StatusPages plugin should handle this and return a 400 Bad Request
        val response = client.post("api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(registerRequest))
        }

        // Verify the registration failed with a Bad Request status
        assertEquals(HttpStatusCode.BadRequest, response.status)

        // Verify the response contains an error message
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("VALIDATION_ERROR"))
    }

    @Test
    fun `test google login - invalid token`() = testApplication {
        // Set development mode to true to skip database configuration in the module
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Try to login with an invalid Google ID token
        val googleLoginRequest = GoogleLoginRequest(
            idToken = "invalid-token"  // Invalid token
        )

        val response = client.post("api/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(googleLoginRequest))
        }

        // Verify the login failed with an Internal Server Error status
        // This is because the GoogleTokenVerifier.verify method throws an IllegalArgumentException
        // when it encounters an invalid token, and this exception is not caught in the UserService
        assertEquals(HttpStatusCode.InternalServerError, response.status)

        // Verify the response contains an error message
        val responseBody = response.bodyAsText()
        assertTrue(responseBody.contains("INTERNAL_SERVER_ERROR"))
    }

    @Test
    fun `test google login - valid token`() = testApplication {
        // Set up the test environment
        val configTest = ApplicationConfig("application_test.conf")

        environment {
            config = configTest
        }

        try {
            // Set up the test Google token verifier
            TestGoogleTokenVerifier.setup()

            // Use the test token that will be accepted by our mock verifier
            val googleLoginRequest = GoogleLoginRequest(
                idToken = TestGoogleTokenVerifier.getTestToken()
            )

            // Make the request
            val response = client.post("api/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(googleLoginRequest))
            }

            // Verify the login was successful
            assertEquals(HttpStatusCode.OK, response.status)

            // Verify the response contains a token and user information
            val responseBody = response.bodyAsText()
            assertTrue(responseBody.contains("token"))
            assertTrue(responseBody.contains("Test User"))
        } finally {
            // Reset the Google token verifier to its original state
            TestGoogleTokenVerifier.reset()
        }
    }
}
