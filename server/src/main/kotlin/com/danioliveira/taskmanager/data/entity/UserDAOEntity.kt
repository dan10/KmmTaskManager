package com.danioliveira.taskmanager.data.entity

import com.danioliveira.taskmanager.data.tables.UsersTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class UserDAOEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAOEntity>(UsersTable)

    var displayName by UsersTable.displayName
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var googleId by UsersTable.googleId
    var createdAt by UsersTable.createdAt
}
