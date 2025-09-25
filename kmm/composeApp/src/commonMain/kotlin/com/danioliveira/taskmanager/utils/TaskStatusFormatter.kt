package com.danioliveira.taskmanager.utils

import androidx.compose.runtime.Composable
import com.danioliveira.taskmanager.domain.TaskStatus
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.task_status_todo
import kmmtaskmanager.composeapp.generated.resources.task_status_in_progress
import kmmtaskmanager.composeapp.generated.resources.task_status_done
import org.jetbrains.compose.resources.stringResource

/**
 * Utility object for formatting TaskStatus values with localized strings.
 */
object TaskStatusFormatter {
    
    /**
     * Returns the localized string for the given task status.
     * 
     * @param status The task status to format
     * @return The localized task status string
     */
    @Composable
    fun formatTaskStatus(status: TaskStatus): String {
        return when (status) {
            TaskStatus.TODO -> stringResource(Res.string.task_status_todo)
            TaskStatus.IN_PROGRESS -> stringResource(Res.string.task_status_in_progress)
            TaskStatus.DONE -> stringResource(Res.string.task_status_done)
        }
    }
} 