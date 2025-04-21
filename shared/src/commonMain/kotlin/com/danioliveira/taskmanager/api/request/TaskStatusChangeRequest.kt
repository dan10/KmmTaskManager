package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for changing the status of a task.
 */
@Serializable
data class TaskStatusChangeRequest(
    val status: String
)