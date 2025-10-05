package com.danioliveira.taskmanager.api.request

import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Request model for updating an existing task.
 */
@Serializable
data class TaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: Priority? = null,
    val dueDate: LocalDateTime? = null,
    val assigneeId: String? = null
)
