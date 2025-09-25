package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object UsersTable : UUIDTable("users") {
    val email = varchar("email", 255)
    val passwordHash = varchar("password_hash", 255).nullable() // null for Google users
    val displayName = varchar("display_name", 255)
    val googleId = varchar("google_id", 255).nullable()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(customIndexName = "uq_users_email", columns = arrayOf(email))
        uniqueIndex(customIndexName = "uq_users_google_id", columns = arrayOf(googleId))
        index(customIndexName = "idx_users_created_at", columns = arrayOf(createdAt))
    }
}
