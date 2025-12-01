package com.danioliveira.taskmanager.api.response

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Response model for a task.
 */
@Serializable
data class TaskResponse(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatusResponse,
    val priority: PriorityResponse,
    val dueDate: LocalDateTime?,
    val projectId: String?,
    val projectName: String?,
    val assigneeId: String?,
    val creatorId: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
