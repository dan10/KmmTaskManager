package com.danioliveira.taskmanager.data.tables

import com.danioliveira.taskmanager.api.response.PriorityResponse
import com.danioliveira.taskmanager.api.response.TaskStatusResponse
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp

object TasksTable : UUIDTable("tasks") {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val projectId = optReference("project_id", ProjectsTable)
    val assigneeId = optReference("assignee_id", UsersTable)
    val creatorId = reference("creator_id", UsersTable)
    val status = enumeration("status", TaskStatusResponse::class).default(TaskStatusResponse.TODO)
    val priority = enumeration("priority", PriorityResponse::class)
    val dueDate = datetime("due_date").nullable()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        index(customIndexName = "idx_tasks_project_id", columns = arrayOf(projectId))
        index(customIndexName = "idx_tasks_assignee_id", columns = arrayOf(assigneeId))
        index(customIndexName = "idx_tasks_creator_id", columns = arrayOf(creatorId))
        index(customIndexName = "idx_tasks_status", columns = arrayOf(status))
        index(customIndexName = "idx_tasks_created_at", columns = arrayOf(createdAt))
    }
}
