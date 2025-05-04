package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

/**
 * Response model for user's task progress.
 */
@Serializable
data class TaskProgressResponse(
    val totalTasks: Int,
    val completedTasks: Int,
)