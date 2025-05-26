package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for updating an existing project.
 */
@Serializable
data class ProjectUpdateRequest(
    val name: String,
    val description: String? = null
)