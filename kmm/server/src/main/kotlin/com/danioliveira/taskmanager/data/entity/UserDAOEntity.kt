package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.UUID

class UserDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAOEntity>(UsersTable)

    var displayName by UsersTable.displayName
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var googleId by UsersTable.googleId
    var createdAt by UsersTable.createdAt
}
