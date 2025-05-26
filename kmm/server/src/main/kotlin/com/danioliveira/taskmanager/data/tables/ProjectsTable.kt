package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object ProjectsTable : UUIDTable() {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val owner = reference("owner", UsersTable)
    val createdAt = datetime("created_at")
}
