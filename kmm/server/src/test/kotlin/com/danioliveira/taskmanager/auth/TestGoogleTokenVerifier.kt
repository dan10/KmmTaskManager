package com.danioliveira.taskmanager.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken

/**
 * Test implementation of GoogleTokenVerifier that returns a valid payload for a specific test token.
 * This allows tests to simulate Google login without actually verifying tokens with Google's servers.
 */
object TestGoogleTokenVerifier {
    private const val TEST_TOKEN = "test-valid-token"
    private const val TEST_EMAIL = "test@example.com"
    private const val TEST_NAME = "Test User"
    private const val TEST_SUBJECT = "123456789"

    /**
     * Replace the real GoogleTokenVerifier with this test implementation.
     * Call this method before running tests that need to mock Google token verification.
     */
    fun setup() {
        // Replace the real GoogleTokenVerifier.verify method with our test implementation
        GoogleTokenVerifier.verifyFunction = { idTokenString: String, _: String ->
            if (idTokenString == TEST_TOKEN) {
                createTestPayload()
            } else {
                null
            }
        }
    }

    /**
     * Reset the GoogleTokenVerifier to use the real implementation.
     * Call this method after tests to clean up.
     */
    fun reset() {
        GoogleTokenVerifier.verifyFunction = null
    }

    /**
     * Create a test payload with predefined values.
     */
    private fun createTestPayload(): GoogleIdToken.Payload {
        return GoogleIdToken.Payload().apply {
            email = TEST_EMAIL
            set("name", TEST_NAME)
            subject = TEST_SUBJECT
        }
    }

    /**
     * Get the test token string that will be accepted by the test verifier.
     */
    fun getTestToken(): String {
        return TEST_TOKEN
    }
}
