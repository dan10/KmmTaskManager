package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

/**
 * Response model for a project.
 */
@Serializable
data class ProjectResponse(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val createdAt: String,
    val completed: Int = 0,
    val inProgress: Int = 0,
    val total: Int = 0
)
