package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatusResponse {
    TODO,
    IN_PROGRESS,
    DONE
}