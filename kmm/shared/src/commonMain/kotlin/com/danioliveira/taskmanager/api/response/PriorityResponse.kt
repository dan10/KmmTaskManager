package com.danioliveira.taskmanager.api.response

import kotlinx.serialization.Serializable

@Serializable
enum class PriorityResponse {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}