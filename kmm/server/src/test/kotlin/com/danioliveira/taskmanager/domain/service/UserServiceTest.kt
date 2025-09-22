package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.BaseServiceTest
import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.auth.PasswordHasher
import com.danioliveira.taskmanager.auth.TestGoogleTokenVerifier
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.UnauthorizedException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.inject
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class UserServiceTest : BaseServiceTest() {
    private val userService: UserService by inject()

    @Test
    fun `test find by email - existing user`() = runTest {
        // Create a user in the database
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        createTestUser(email, passwordHash, displayName)

        // Find the user by email
        val user = userService.findByEmail(email)

        // Verify the user was found
        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(passwordHash, user.passwordHash)
    }

    @Test
    fun `test find by email - non-existent user`() = runTest {
        // Try to find a user that doesn't exist
        val user = userService.findByEmail("nonexistent@example.com")

        // Verify the user was not found
        assertNull(user)
    }

    @Test
    fun `test find by id - existing user`() = runTest {
        // Create a user in the database
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        val userId = UUID.fromString(createTestUser(email, passwordHash, displayName))

        // Find the user by ID
        val user = userService.findById(userId)

        // Verify the user was found
        assertNotNull(user)
        assertEquals(userId, UUID.fromString(user.id))
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
    }

    @Test
    fun `test find by id - non-existent user`() = runTest {
        // Try to find a user that doesn't exist
        try {
            userService.findById(UUID.randomUUID())
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("User"))
        }
    }

    @Test
    fun `test create user`() = runTest {
        // Create a user
        val email = "newuser@example.com"
        val displayName = "New User"
        val passwordHash = "hashed-password"

        val user = userService.create(email, passwordHash, displayName, null)

        // Verify the user was created correctly
        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(passwordHash, user.passwordHash)

        // Verify the user can be found in the database
        val foundUser = userService.findByEmail(email)
        assertNotNull(foundUser)
        assertEquals(user.id, foundUser.id)
    }

    @Test
    fun `test to safe user`() = runTest {
        // Create a user
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        val userId = UUID.fromString(createTestUser(email, passwordHash, displayName))
        val user = userService.findById(userId)

        // Convert to safe user
        val safeUser = userService.toSafeUser(user)

        // Verify the safe user doesn't have a password hash
        assertEquals(user.id, safeUser.id)
        assertEquals(email, safeUser.email)
        assertEquals(displayName, safeUser.displayName)
    }


    @Test
    fun `test login - successful`() = runTest {
        // Create a user in the database
        val email = "test@example.com"
        val password = "password123"
        val displayName = "Test User"
        val passwordHash = PasswordHasher.hash(password)

        createTestUser(email, passwordHash, displayName)

        // Create a login request
        val request = LoginRequest(
            email = email,
            password = password
        )

        // Login the user
        val response = userService.login(request)

        // Verify the response contains a token and user information
        assertNotNull(response.token)
        assertNotNull(response.user)
        assertEquals(email, response.user.email)
        assertEquals(displayName, response.user.displayName)
    }

    @Test
    fun `test login - invalid email`() = runTest {
        // Create a login request with an email that doesn't exist
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        )

        // This should throw an UnauthorizedException
        try {
            userService.login(request)
            fail("Expected UnauthorizedException was not thrown")
        } catch (e: UnauthorizedException) {
            // Expected exception
            assertTrue(e.message.contains("Invalid email or password"))
        }
    }

    @Test
    fun `test login - invalid password`() = runTest {
        // Create a user in the database
        val email = "test@example.com"
        val password = "password123"
        val displayName = "Test User"
        val passwordHash = PasswordHasher.hash(password)

        createTestUser(email, passwordHash, displayName)

        // Create a login request with the wrong password
        val request = LoginRequest(
            email = email,
            password = "wrongpassword"
        )

        // This should throw an UnauthorizedException
        try {
            userService.login(request)
            fail("Expected UnauthorizedException was not thrown")
        } catch (e: UnauthorizedException) {
            // Expected exception
            assertTrue(e.message.contains("Invalid email or password"))
        }
    }

    @Test
    fun `test google login - valid token`() = runTest {
        try {
            // Set up the test Google token verifier
            TestGoogleTokenVerifier.setup()

            // Create a Google login request with the test token
            val request = GoogleLoginRequest(
                idToken = TestGoogleTokenVerifier.getTestToken()
            )

            // Login the user
            val response = userService.googleLogin(request)

            // Verify the response contains a token and user information
            assertNotNull(response.token)
            assertNotNull(response.user)
            assertEquals("test@example.com", response.user.email)
            assertEquals("Test User", response.user.displayName)
            assertEquals("123456789", response.user.googleId)

            // Verify the user was created in the database
            val user = userService.findByEmail("test@example.com")
            assertNotNull(user)
            assertEquals("test@example.com", user.email)
            assertEquals("Test User", user.displayName)
            assertEquals("123456789", user.googleId)
            assertNull(user.passwordHash)
        } finally {
            // Reset the Google token verifier
            TestGoogleTokenVerifier.reset()
        }
    }

    @Test
    fun `test google login - invalid token`() = runTest {
        try {
            // Set up the test Google token verifier
            TestGoogleTokenVerifier.setup()

            // Create a Google login request with an invalid token
            val request = GoogleLoginRequest(
                idToken = "invalid-token"
            )

            // This should throw an UnauthorizedException
            try {
                userService.googleLogin(request)
                fail("Expected UnauthorizedException was not thrown")
            } catch (e: UnauthorizedException) {
                // Expected exception
                assertTrue(e.message.contains("Invalid Google ID token"))
            }
        } finally {
            // Reset the Google token verifier
            TestGoogleTokenVerifier.reset()
        }
    }

    @Test
    fun `test google login - existing user`() = runTest {
        try {
            // Create a user in the database with the same email as the test token
            val email = "test@example.com"
            val displayName = "Existing User"
            val googleId = "existing-google-id"

            createTestUser(email, "", displayName, googleId)

            // Set up the test Google token verifier
            TestGoogleTokenVerifier.setup()

            // Create a Google login request with the test token
            val request = GoogleLoginRequest(
                idToken = TestGoogleTokenVerifier.getTestToken()
            )

            // Login the user
            val response = userService.googleLogin(request)

            // Verify the response contains a token and user information
            assertNotNull(response.token)
            assertNotNull(response.user)
            assertEquals(email, response.user.email)

            // The user should still have the original display name and Google ID
            assertEquals(displayName, response.user.displayName)
            assertEquals(googleId, response.user.googleId)
        } finally {
            // Reset the Google token verifier
            TestGoogleTokenVerifier.reset()
        }
    }
}
