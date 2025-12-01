package com.danioliveira.taskmanager.api.request

import com.danioliveira.taskmanager.api.response.PriorityResponse
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Request model for creating a new task.
 */
@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    val projectId: String? = null,
    val assigneeId: String? = null,
    val priority: PriorityResponse,
    val dueDate: LocalDateTime? = null
)
