package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import org.jetbrains.exposed.v1.core.Transaction
import java.util.UUID

interface UserRepository {

    context(transaction: Transaction)
    suspend fun findByEmail(email: String): UserWithPassword?

    context(transaction: Transaction)
    suspend fun findById(id: UUID): UserWithPassword?

    context(transaction: Transaction)
    suspend fun existsById(id: UUID): Boolean

    context(transaction: Transaction)
    suspend fun create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword

    fun toSafeUser(user: UserWithPassword): User
}
