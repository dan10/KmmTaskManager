package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface UserRepository {

    context(transaction: R2dbcTransaction)
    suspend fun findByEmail(email: String): UserWithPassword?

    context(transaction: R2dbcTransaction)
    suspend fun findById(id: Uuid): UserWithPassword?

    context(transaction: R2dbcTransaction)
    suspend fun existsById(id: Uuid): Boolean

    context(transaction: R2dbcTransaction)
    suspend fun create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword

    fun toSafeUser(user: UserWithPassword): User
}
