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
    val createdAt: String
)