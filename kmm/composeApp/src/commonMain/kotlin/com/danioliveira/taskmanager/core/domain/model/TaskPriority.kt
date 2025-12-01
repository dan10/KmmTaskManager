package com.danioliveira.taskmanager.core.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt

enum class TaskPriority(
    @Deprecated("Use TaskPriority.getColors() composable instead", ReplaceWith("TaskPriority.getColors()"))
    val color: Color,
    @Deprecated("Use TaskPriority.getColors() composable instead", ReplaceWith("TaskPriority.getColors()"))
    val backgroundColor: Color
) {
    NONE(
        color = Color(0xFF6B7280),          // Gray
        backgroundColor = Color(0xFFF3F4F6), // Light Gray
    ),
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
    
    /**
     * Gets the text and container colors for this priority from the theme.
     * Use this instead of the deprecated color and backgroundColor properties.
     */
    @Composable
    fun getColors(extendedColors: TaskItExtendedColors = TaskItThemeExt.colors): PriorityColors {
        return when (this) {
            NONE -> PriorityColors(
                text = extendedColors.priorityNoneText,
                container = extendedColors.priorityNoneContainer
            )
            HIGH -> PriorityColors(
                text = extendedColors.priorityHighText,
                container = extendedColors.priorityHighContainer
            )
            MEDIUM -> PriorityColors(
                text = extendedColors.priorityMediumText,
                container = extendedColors.priorityMediumContainer
            )
            LOW -> PriorityColors(
                text = extendedColors.priorityLowText,
                container = extendedColors.priorityLowContainer
            )
        }
    }
}

/**
 * Data class holding priority colors from theme.
 */
data class PriorityColors(
    val text: Color,
    val container: Color
)

fun Priority.toTaskPriority(): TaskPriority {
    return when (this) {
        Priority.NONE -> TaskPriority.NONE
        Priority.HIGH -> TaskPriority.HIGH
        Priority.MEDIUM -> TaskPriority.MEDIUM
        Priority.LOW -> TaskPriority.LOW
    }
}