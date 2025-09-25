package com.danioliveira.taskmanager.auth

import java.security.MessageDigest
import java.util.*

object PasswordHasher {
    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hash)
    }

    fun verify(password: String, hash: String): Boolean = this.hash(password) == hash
}
