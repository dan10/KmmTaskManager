package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskPriority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

data class TaskItemState(
    val task: Task,
    val colors: TaskItemColors
) {
    val priority = task.priority.toTaskPriority()
    val isOverdue = task.isOverdue()

    @OptIn(ExperimentalTime::class)
    private fun Task.isOverdue(): Boolean {
        return dueDate?.let { dueDate ->
            val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            status != TaskStatus.DONE && dueDate < now
        } ?: false
    }
}

// Remember and compute task item state
@Composable
fun rememberTaskItemState(task: Task): TaskItemState {
    val extendedColors = TaskItThemeExt.colors
    val priority = task.priority.toTaskPriority()
    val isOverdue = task.isOverdue()
    
    return remember(task, extendedColors) {
        TaskItemState(
            task = task,
            colors = createTaskItemColors(
                task = task,
                priority = priority,
                isOverdue = isOverdue,
                extendedColors = extendedColors
            )
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun Task.isOverdue(): Boolean {
    return dueDate?.let { dueDate ->
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        status != TaskStatus.DONE && dueDate < now
    } ?: false
}

private fun createTaskItemColors(
    task: Task,
    priority: TaskPriority,
    isOverdue: Boolean,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): TaskItemColors {
    return TaskItemColors(
        indicator = getIndicatorColor(priority, isOverdue, extendedColors),
        container = getContainerColor(task, isOverdue, extendedColors),
        title = getTitleColor(task, extendedColors),
        description = getDescriptionColor(task, extendedColors),
        statusText = getStatusTextColor(task.status, extendedColors),
        statusBackground = getStatusBackgroundColor(task.status, extendedColors)
    )
}

private fun getIndicatorColor(
    priority: TaskPriority,
    isOverdue: Boolean,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return when {
        isOverdue -> extendedColors.taskIndicatorOverdue
        else -> priority.color
    }
}

private fun getContainerColor(
    task: Task,
    isOverdue: Boolean,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return when {
        isOverdue -> extendedColors.taskContainerOverdue
        task.status == TaskStatus.DONE -> extendedColors.taskContainerDone
        else -> extendedColors.taskContainerDefault
    }
}

private fun getTitleColor(
    task: Task,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return if (task.status == TaskStatus.DONE) {
        extendedColors.taskTitleDone
    } else {
        extendedColors.taskTitleDefault
    }
}

private fun getDescriptionColor(
    task: Task,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return if (task.status == TaskStatus.DONE) {
        extendedColors.taskDescriptionDone
    } else {
        extendedColors.taskDescriptionDefault
    }
}

private fun getStatusTextColor(
    status: TaskStatus,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return when (status) {
        TaskStatus.TODO -> extendedColors.statusTodoText
        TaskStatus.IN_PROGRESS -> extendedColors.statusInProgressText
        TaskStatus.DONE -> extendedColors.statusDoneText
    }
}

private fun getStatusBackgroundColor(
    status: TaskStatus,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
): Color {
    return when (status) {
        TaskStatus.TODO -> extendedColors.statusTodoContainer
        TaskStatus.IN_PROGRESS -> extendedColors.statusInProgressContainer
        TaskStatus.DONE -> extendedColors.statusDoneContainer
    }
}

// Extension function to check if task is overdue

// Data class to hold all colors for a task item
data class TaskItemColors(
    val indicator: Color,
    val container: Color,
    val title: Color,
    val description: Color,
    val statusText: Color,
    val statusBackground: Color
)