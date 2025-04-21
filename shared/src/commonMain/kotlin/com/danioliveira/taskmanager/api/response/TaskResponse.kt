package com.danioliveira.taskmanager.api.response

import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import kotlinx.serialization.Serializable

/**
 * Response model for a task.
 */
@Serializable
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: String,
    val projectId: String?,
    val assigneeId: String?,
    val creatorId: String
)
