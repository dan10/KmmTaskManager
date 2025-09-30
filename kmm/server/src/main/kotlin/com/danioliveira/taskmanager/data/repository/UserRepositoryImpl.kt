package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import com.danioliveira.taskmanager.domain.repository.UserRepository
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import java.util.UUID
import kotlin.time.ExperimentalTime

internal class UserRepositoryImpl : UserRepository {

    context(transaction: R2dbcTransaction)
    override suspend fun findByEmail(email: String): UserWithPassword? = with(transaction) {
        return UsersTable
            .selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?.toDomain()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findById(id: UUID): UserWithPassword? = with(transaction) {
        return UsersTable
            .selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toDomain()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun existsById(id: UUID): Boolean = with(transaction) {
        return UsersTable
            .select(UsersTable.id)
            .where { UsersTable.id eq id }
            .singleOrNull() != null
    }

    @OptIn(ExperimentalTime::class)
    context(transaction: R2dbcTransaction)
    override suspend fun create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword = with(transaction) {
        val row = UsersTable.insertReturning {
            it[this.email] = email
            it[this.passwordHash] = passwordHash
            it[this.displayName] = displayName
            it[this.googleId] = googleId
        }.single()
        return row.toDomain()
    }

    override fun toSafeUser(user: UserWithPassword): User = User(
        id = user.id,
        email = user.email,
        displayName = user.displayName,
        googleId = user.googleId,
        createdAt = user.createdAt
    )

    context(_ : UserRepository)
    private fun ResultRow.toDomain() = UserWithPassword(
        id = this[UsersTable.id].value.toString(),
        email = this[UsersTable.email],
        displayName = this[UsersTable.displayName],
        googleId = this[UsersTable.googleId],
        createdAt = this[UsersTable.createdAt].toString(),
        passwordHash = this[UsersTable.passwordHash]
    )

}
