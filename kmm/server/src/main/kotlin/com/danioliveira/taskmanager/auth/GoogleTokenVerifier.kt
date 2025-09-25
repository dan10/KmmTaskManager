package com.danioliveira.taskmanager.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory

object GoogleTokenVerifier {
    private val transport = NetHttpTransport()

    private val jsonFactory = GsonFactory.getDefaultInstance()

    // Function type for verification to allow mocking in tests
    internal var verifyFunction: ((String, String) -> GoogleIdToken.Payload?)? = null

    fun verify(idTokenString: String, clientId: String): GoogleIdToken.Payload? {
        // If a test has set a custom verification function, use it
        verifyFunction?.let { return it(idTokenString, clientId) }

        // Otherwise, use the real implementation
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(listOf(clientId))
            .build()

        val idToken = verifier.verify(idTokenString)
        return idToken?.payload
    }
}
