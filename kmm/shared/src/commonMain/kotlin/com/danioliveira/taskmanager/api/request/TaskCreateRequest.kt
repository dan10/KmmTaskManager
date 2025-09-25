package com.danioliveira.taskmanager.api.request

import com.danioliveira.taskmanager.domain.Priority
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
    val priority: Priority,
    val dueDate: LocalDateTime? = null
)
