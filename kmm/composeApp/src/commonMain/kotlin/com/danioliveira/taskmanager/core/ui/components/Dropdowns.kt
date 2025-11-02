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
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_dropdown_arrow
import kmmtaskmanager.composeapp.generated.resources.content_description_select_priority
import kmmtaskmanager.composeapp.generated.resources.task_priority_label
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable dropdown component for Priority selection.
 *
 * Displays a dropdown menu allowing users to select a task priority level.
 *
 * @param currentPriority The currently selected priority
 * @param expanded Whether the dropdown menu is expanded
 * @param onExpandedChange Callback when dropdown expansion state changes
 * @param onPrioritySelected Callback when a priority is selected
 * @param modifier Modifier to be applied to the dropdown
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
            label = { Text(stringResource(Res.string.task_priority_label)) },
            trailingIcon = {
                IconButton(onClick = { onExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(Res.string.content_description_select_priority)
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
 *
 * Displays a dropdown menu allowing users to select a task status.
 *
 * @param currentStatus The currently selected status
 * @param expanded Whether the dropdown menu is expanded
 * @param onExpandedChange Callback when dropdown expansion state changes
 * @param onStatusSelected Callback when a status is selected
 * @param modifier Modifier to be applied to the dropdown
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


@Preview
@Composable
private fun TaskItPriorityDropdownPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var expanded1 by remember { mutableStateOf(false) }
            var priority1 by remember { mutableStateOf(Priority.MEDIUM) }
            
            Text(
                text = "Priority Dropdown - Collapsed",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            TaskItPriorityDropdown(
                currentPriority = priority1,
                expanded = expanded1,
                onExpandedChange = { expanded1 = it },
                onPrioritySelected = { priority1 = it }
            )
            
            var expanded2 by remember { mutableStateOf(false) }
            var priority2 by remember { mutableStateOf(Priority.HIGH) }
            
            Text(
                text = "Priority Dropdown - High Priority",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
            TaskItPriorityDropdown(
                currentPriority = priority2,
                expanded = expanded2,
                onExpandedChange = { expanded2 = it },
                onPrioritySelected = { priority2 = it }
            )
            
            var expanded3 by remember { mutableStateOf(false) }
            var priority3 by remember { mutableStateOf(Priority.LOW) }
            
            Text(
                text = "Priority Dropdown - Low Priority",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
            TaskItPriorityDropdown(
                currentPriority = priority3,
                expanded = expanded3,
                onExpandedChange = { expanded3 = it },
                onPrioritySelected = { priority3 = it }
            )
        }
    }
}

@Preview
@Composable
private fun TaskItStatusDropdownPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            var expanded1 by remember { mutableStateOf(false) }
            var status1 by remember { mutableStateOf(TaskStatus.TODO) }
            
            Text(
                text = "Status Dropdown - To Do",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            TaskItStatusDropdown(
                currentStatus = status1,
                expanded = expanded1,
                onExpandedChange = { expanded1 = it },
                onStatusSelected = { status1 = it }
            )
            
            var expanded2 by remember { mutableStateOf(false) }
            var status2 by remember { mutableStateOf(TaskStatus.IN_PROGRESS) }
            
            Text(
                text = "Status Dropdown - In Progress",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
            TaskItStatusDropdown(
                currentStatus = status2,
                expanded = expanded2,
                onExpandedChange = { expanded2 = it },
                onStatusSelected = { status2 = it }
            )
            
            var expanded3 by remember { mutableStateOf(false) }
            var status3 by remember { mutableStateOf(TaskStatus.DONE) }
            
            Text(
                text = "Status Dropdown - Done",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
            )
            TaskItStatusDropdown(
                currentStatus = status3,
                expanded = expanded3,
                onExpandedChange = { expanded3 = it },
                onStatusSelected = { status3 = it }
            )
        }
    }
}

@Preview
@Composable
private fun TaskItDropdownsInteractivePreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Interactive Dropdowns",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            var priorityExpanded by remember { mutableStateOf(false) }
            var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
            
            TaskItPriorityDropdown(
                currentPriority = selectedPriority,
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = it },
                onPrioritySelected = { 
                    selectedPriority = it
                    priorityExpanded = false
                }
            )
            
            var statusExpanded by remember { mutableStateOf(false) }
            var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
            
            TaskItStatusDropdown(
                currentStatus = selectedStatus,
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it },
                onStatusSelected = { 
                    selectedStatus = it
                    statusExpanded = false
                }
            )
            
            Text(
                text = "Selected: ${PriorityFormatter.formatPriority(selectedPriority)} - ${TaskStatusFormatter.formatTaskStatus(selectedStatus)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
 