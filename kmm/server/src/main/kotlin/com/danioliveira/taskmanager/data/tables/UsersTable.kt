package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.datetime

object UsersTable : UUIDTable() {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable() // null for Google users
    val displayName = varchar("display_name", 255)
    val googleId = varchar("google_id", 255).nullable()
    val createdAt = datetime("created_at")
}
