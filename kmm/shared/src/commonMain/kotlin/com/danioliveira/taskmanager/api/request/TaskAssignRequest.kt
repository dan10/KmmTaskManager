package com.danioliveira.taskmanager.api.request

import kotlinx.serialization.Serializable

/**
 * Request model for assigning a task to a user.
 */
@Serializable
data class TaskAssignRequest(
    val assigneeId: String
)