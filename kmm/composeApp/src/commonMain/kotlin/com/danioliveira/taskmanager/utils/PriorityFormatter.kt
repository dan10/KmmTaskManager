package com.danioliveira.taskmanager.utils

import androidx.compose.runtime.Composable
import com.danioliveira.taskmanager.domain.Priority
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.priority_high
import kmmtaskmanager.composeapp.generated.resources.priority_low
import kmmtaskmanager.composeapp.generated.resources.priority_medium
import org.jetbrains.compose.resources.stringResource

/**
 * Utility object for formatting Priority values with localized strings.
 */
object PriorityFormatter {

    /**
     * Returns the localized string for the given priority.
     *
     * @param priority The priority to format
     * @return The localized priority string
     */
    @Composable
    fun formatPriority(priority: Priority): String {
        return when (priority) {
            Priority.HIGH -> stringResource(Res.string.priority_high)
            Priority.MEDIUM -> stringResource(Res.string.priority_medium)
            Priority.LOW -> stringResource(Res.string.priority_low)
        }
    }
} 