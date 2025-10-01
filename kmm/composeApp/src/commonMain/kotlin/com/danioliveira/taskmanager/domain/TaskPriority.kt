package com.danioliveira.taskmanager.domain

import androidx.compose.ui.graphics.Color

enum class TaskPriority(
    val color: Color,
    val backgroundColor: Color
) {
    HIGH(
        color = Color(0xFFDC2626),          // Bright Red
        backgroundColor = Color(0xFFFFE4E4), // Light Red
    ),
    MEDIUM(
        color = Color(0xFFEAB308),          // Bright Yellow
        backgroundColor = Color(0xFFFEF9C3), // Light Yellow
    ),
    LOW(
        color = Color(0xFF22C55E),          // Bright Green
        backgroundColor = Color(0xFFDCFCE7), // Light Green
    );
}

fun Priority.toTaskPriority(): TaskPriority {
    return when (this) {
        Priority.HIGH -> TaskPriority.HIGH
        Priority.MEDIUM -> TaskPriority.MEDIUM
        Priority.LOW -> TaskPriority.LOW
    }
}