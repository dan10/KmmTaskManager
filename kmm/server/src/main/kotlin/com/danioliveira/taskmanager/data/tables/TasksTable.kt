package com.danioliveira.taskmanager.data.tables

import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.datetime

object TasksTable : UUIDTable() {
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val project = optReference("project", ProjectsTable)
    val assignee = optReference("assignee", UsersTable)
    val creator = reference("creator", UsersTable)
    val status = enumeration("status", TaskStatus::class)
    val priority = enumeration("priority", Priority::class)

    val dueDate = datetime("due_date").nullable()
    val createdAt = datetime("created_at")
}
