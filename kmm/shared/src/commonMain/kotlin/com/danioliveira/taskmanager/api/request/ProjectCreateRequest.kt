package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for creating a new project.
 */
@Serializable
data class ProjectCreateRequest(
    val name: String,
    val description: String? = null
)