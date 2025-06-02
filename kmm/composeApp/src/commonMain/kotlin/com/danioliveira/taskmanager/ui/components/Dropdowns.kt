package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
                    onClick = {
                        onPrioritySelected(priorityOption)
                        onExpandedChange(false)
                    }
                ) {
                    Text(PriorityFormatter.formatPriority(priorityOption))
                }
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
                    onClick = {
                        onStatusSelected(statusOption)
                        onExpandedChange(false)
                    }
                ) {
                    Text(TaskStatusFormatter.formatTaskStatus(statusOption))
                }
            }
        }
    }
}

/**
 * Generic dropdown component for any enum type.
 */
@Composable
fun <T> TaskItDropdown(
    currentValue: T,
    options: List<T>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueSelected: (T) -> Unit,
    valueFormatter: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onExpandedChange(true) },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(valueFormatter(currentValue))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onValueSelected(option)
                        onExpandedChange(false)
                    }
                ) {
                    Text(valueFormatter(option))
                }
            }
        }
    }
} 