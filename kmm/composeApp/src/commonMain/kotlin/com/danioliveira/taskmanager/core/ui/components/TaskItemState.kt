package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskPriority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

data class TaskItemState(val task: Task) {

    val priority = task.priority.toTaskPriority()
    val isOverdue = task.isOverdue()
    val colors = createTaskItemColors(task, priority, isOverdue)

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
        isOverdue: Boolean
    ): TaskItemColors {
        return TaskItemColors(
            indicator = getIndicatorColor(priority, isOverdue),
            container = getContainerColor(task, isOverdue),
            title = getTitleColor(task),
            description = getDescriptionColor(task),
            statusText = getStatusTextColor(task.status),
            statusBackground = getStatusBackgroundColor(task.status)
        )
    }

    private fun getIndicatorColor(priority: TaskPriority, isOverdue: Boolean): Color {
        return when {
            isOverdue -> Color(0xFFEF4444)
            else -> priority.color
        }
    }

    private fun getContainerColor(task: Task, isOverdue: Boolean): Color {
        return when {
            isOverdue -> Color(0xFFFFF5F5)
            task.status == TaskStatus.DONE -> Color(0xFFF9FAFB)
            else -> Color.White
        }
    }

    private fun getTitleColor(task: Task): Color {
        return if (task.status == TaskStatus.DONE) {
            Color(0xFF9CA3AF)
        } else {
            Color(0xFF1A1A1A)
        }
    }

    private fun getDescriptionColor(task: Task): Color {
        return if (task.status == TaskStatus.DONE) {
            Color(0xFFD1D5DB)
        } else {
            Color(0xFF6B7280)
        }
    }

    private fun getStatusTextColor(status: TaskStatus): Color {
        return when (status) {
            TaskStatus.TODO -> Color(0xFF6B7280)
            TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
            TaskStatus.DONE -> Color(0xFF10B981)
        }
    }

    private fun getStatusBackgroundColor(status: TaskStatus): Color {
        return when (status) {
            TaskStatus.TODO -> Color(0xFFF3F4F6)
            TaskStatus.IN_PROGRESS -> Color(0xFFDCEEFE)
            TaskStatus.DONE -> Color(0xFFD1FAE5)
        }
    }
}

// Remember and compute task item state
@Composable
fun rememberTaskItemState(task: Task): TaskItemState {
    return remember(task) {
        TaskItemState(task = task)
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