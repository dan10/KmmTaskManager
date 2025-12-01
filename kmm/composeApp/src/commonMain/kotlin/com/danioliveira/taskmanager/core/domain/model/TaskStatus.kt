package com.danioliveira.taskmanager.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}