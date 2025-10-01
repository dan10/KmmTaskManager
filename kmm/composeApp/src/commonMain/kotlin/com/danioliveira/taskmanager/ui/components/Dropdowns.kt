package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter

/**
 * Reusable dropdown component for Priority selection.
 */
@Composable
fun TaskItPriorityDropdown(
    currentPriority: Priority,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onExpandedChange(true) },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(PriorityFormatter.formatPriority(currentPriority))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            Priority.entries.forEach { priorityOption ->
                DropdownMenuItem(
                    text = { Text(PriorityFormatter.formatPriority(priorityOption)) },
                    onClick = {
                        onPrioritySelected(priorityOption)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

/**
 * Reusable dropdown component for TaskStatus selection.
 */
@Composable
fun TaskItStatusDropdown(
    currentStatus: TaskStatus,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onStatusSelected: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onExpandedChange(true) },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(TaskStatusFormatter.formatTaskStatus(currentStatus))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            TaskStatus.entries.forEach { statusOption ->
                DropdownMenuItem(
                    text = { Text(TaskStatusFormatter.formatTaskStatus(statusOption)) },
                    onClick = {
                        onStatusSelected(statusOption)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
} 