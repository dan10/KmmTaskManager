package com.danioliveira.taskmanager.api.request

import com.danioliveira.taskmanager.domain.TaskStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Request model for updating an existing task within a project.
 * Used with the project-specific task routes.
 */
@Serializable
data class ProjectTaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val dueDate: LocalDateTime? = null,
    val assigneeId: String? = null
)