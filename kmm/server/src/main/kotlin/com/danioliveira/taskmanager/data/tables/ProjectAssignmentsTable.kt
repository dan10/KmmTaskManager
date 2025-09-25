package com.danioliveira.taskmanager.data.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object ProjectAssignmentsTable : UUIDTable("project_assignments") {

    val projectId = reference("project_id",ProjectsTable)

    val userId = reference("user_id", UsersTable)

    val assignedAt = datetime("assigned_at").defaultExpression(CurrentDateTime)
    val assignedBy = reference("assigned_by", UsersTable)

    init {
        uniqueIndex(customIndexName = "uq_project_assignments_project_user", columns = arrayOf(projectId, userId))
        index(customIndexName = "idx_project_assignments_project_id", columns = arrayOf(projectId))
        index(customIndexName = "idx_project_assignments_user_id", columns = arrayOf(userId))
        index(customIndexName = "idx_project_assignments_assigned_at", columns = arrayOf(assignedAt))
    }
}
