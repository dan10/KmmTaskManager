package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.data.entity.UserDAOEntity
import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import com.danioliveira.taskmanager.domain.repository.UserRepository
import org.jetbrains.exposed.sql.Transaction
import java.time.LocalDateTime
import java.util.*

internal class UserRepositoryImpl : UserRepository {

    override suspend fun Transaction.findByEmail(email: String): UserWithPassword? =
        UserDAOEntity.find { UsersTable.email eq email }.singleOrNull()?.toDomain()

    override suspend fun Transaction.findById(id: String): UserWithPassword? =
        UserDAOEntity.findById(UUID.fromString(id))?.toDomain()

    override suspend fun Transaction.create(
        email: String,
        passwordHash: String?,
        displayName: String,
        googleId: String?
    ): UserWithPassword {
        val entity = UserDAOEntity.new {
            this.email = email
            this.passwordHash = passwordHash
            this.displayName = displayName
            this.googleId = googleId
            this.createdAt = LocalDateTime.now()
        }
        return entity.toDomain()
    }

    override fun toSafeUser(user: UserWithPassword): User = User(
        id = user.id,
        email = user.email,
        displayName = user.displayName,
        googleId = user.googleId,
        createdAt = user.createdAt
    )

    private fun UserDAOEntity.toDomain() = UserWithPassword(
        id = this.id.value.toString(),
        email = this.email,
        displayName = this.displayName,
        googleId = this.googleId,
        createdAt = this.createdAt.toString(),
        passwordHash = this.passwordHash
    )
}
