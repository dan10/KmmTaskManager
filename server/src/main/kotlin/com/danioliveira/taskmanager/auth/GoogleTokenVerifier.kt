package com.danioliveira.taskmanager.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.serialization.json.Json

object GoogleTokenVerifier {
    private val transport = NetHttpTransport()
    private val fac = Json

    private val jsonFactory = GsonFactory.getDefaultInstance()

    fun verify(idTokenString: String, clientId: String): GoogleIdToken.Payload? {
        val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(listOf(clientId))
            .build()

        val idToken = verifier.verify(idTokenString)
        return idToken?.payload
    }
}
