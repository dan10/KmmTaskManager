package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}