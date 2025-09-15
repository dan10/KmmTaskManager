package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object ProjectsTable : UUIDTable("projects") {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val ownerId = reference("owner_id", UsersTable)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        index(customIndexName = "idx_projects_owner_id", columns = arrayOf(ownerId))
        index(customIndexName = "idx_projects_created_at", columns = arrayOf(createdAt))
        uniqueIndex(customIndexName = "uq_projects_name_owner", columns = arrayOf(name, ownerId))
    }
}
