package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime

object ProjectAssignmentsTable : UUIDTable() {
    val project = reference("project", ProjectsTable)
    val user = reference("user", UsersTable)
    val assignedAt = datetime("assigned_at")
}