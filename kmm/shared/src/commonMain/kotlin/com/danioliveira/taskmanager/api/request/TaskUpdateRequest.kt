package com.danioliveira.taskmanager.api.request

import com.danioliveira.taskmanager.api.response.PriorityResponse
import com.danioliveira.taskmanager.api.response.TaskStatusResponse
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Request model for updating an existing task.
 */
@Serializable
data class TaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatusResponse? = null,
    val priority: PriorityResponse? = null,
    val dueDate: LocalDateTime? = null,
    val assigneeId: String? = null
)
