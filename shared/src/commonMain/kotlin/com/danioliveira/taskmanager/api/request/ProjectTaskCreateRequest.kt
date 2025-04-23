package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for creating a new task within a project.
 * The projectId is provided in the URL path, so it's not included here.
 */
@Serializable
data class ProjectTaskCreateRequest(
    val title: String,
    val description: String? = null,
    val assigneeId: String? = null,
    val status: String = "TODO",
    val dueDate: String? = null
)