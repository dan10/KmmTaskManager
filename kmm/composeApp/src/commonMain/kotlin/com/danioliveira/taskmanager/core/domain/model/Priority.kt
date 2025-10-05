package com.danioliveira.taskmanager.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}
