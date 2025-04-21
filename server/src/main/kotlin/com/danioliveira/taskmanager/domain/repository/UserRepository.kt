package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.model.UserWithPassword
import org.jetbrains.exposed.sql.Transaction

interface UserRepository {
    suspend fun Transaction.findByEmail(email: String): UserWithPassword?
    suspend fun Transaction.findById(id: String): UserWithPassword?
    suspend fun Transaction.create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword

    fun toSafeUser(user: UserWithPassword): User
}
