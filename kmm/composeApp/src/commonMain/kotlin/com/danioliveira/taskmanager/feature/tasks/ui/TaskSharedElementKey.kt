package com.danioliveira.taskmanager.feature.tasks.ui

data class TaskSharedElementKey(
    val taskId: String,
    val type: TaskSharedElementType
)

enum class TaskSharedElementType {
    Bounds
}
