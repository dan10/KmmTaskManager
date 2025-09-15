package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.datetime

object ProjectAssignmentsTable : UUIDTable() {
    val project = reference(name = "project", ProjectsTable)
    val user = reference("user", UsersTable)
    val assignedAt = datetime("assigned_at")

    init {
        index(true, project, user)
    }
}
