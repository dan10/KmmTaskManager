package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for creating a new task.
 */
@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    val projectId: String? = null,
    val assigneeId: String,
    val status: String = "TODO",
    val dueDate: String? = null
)
