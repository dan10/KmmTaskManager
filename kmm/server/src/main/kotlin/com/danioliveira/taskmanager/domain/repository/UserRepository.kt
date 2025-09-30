package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import java.util.UUID

interface UserRepository {

    context(transaction: R2dbcTransaction)
    suspend fun findByEmail(email: String): UserWithPassword?

    context(transaction: R2dbcTransaction)
    suspend fun findById(id: UUID): UserWithPassword?

    context(transaction: R2dbcTransaction)
    suspend fun existsById(id: UUID): Boolean

    context(transaction: R2dbcTransaction)
    suspend fun create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword

    fun toSafeUser(user: UserWithPassword): User
}
