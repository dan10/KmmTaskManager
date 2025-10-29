package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
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
    val interactionSource = remember { MutableInteractionSource() }
    
    // Handle clicks on the text field
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onExpandedChange(true)
            }
        }
    }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = PriorityFormatter.formatPriority(currentPriority),
            onValueChange = {},
            readOnly = true,
            label = { Text("Priority") },
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select priority"
                    )
                }
            },
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth()
        )

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