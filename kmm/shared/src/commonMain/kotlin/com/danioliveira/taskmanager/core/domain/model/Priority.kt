package com.danioliveira.taskmanager.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}
