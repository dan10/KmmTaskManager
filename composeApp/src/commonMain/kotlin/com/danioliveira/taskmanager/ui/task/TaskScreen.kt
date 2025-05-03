package com.danioliveira.taskmanager.ui.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TaskCreatEditScreen(
    taskId: String? = null,
    isCreating: Boolean = false,
    onBack: () -> Unit = {},
    onTaskCreated: () -> Unit = {},
    onTaskUpdated: () -> Unit = {},
    onTaskDeleted: () -> Unit = {}
) {
    // In a real app, you would fetch the task from a repository or ViewModel
    // For now, we'll use a dummy task or create a new one
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.TODO) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf("") }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var priorityDropdownExpanded by remember { mutableStateOf(false) }

    // Initialize with dummy data if not creating
    if (!isCreating && taskId != null) {
        title = "Task $taskId"
        description = "This is a task description"
        status = TaskStatus.TODO
        priority = Priority.MEDIUM
        dueDate = "2023-12-31"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCreating) "Create Task" else "Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isCreating) {
                        IconButton(onClick = onTaskDeleted) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status dropdown
            Text(
                text = "Status",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedButton(
                onClick = { statusDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(status.name)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)

                DropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    TaskStatus.values().forEach { statusOption ->
                        DropdownMenuItem(
                            onClick = {
                                status = statusOption
                                statusDropdownExpanded = false
                            }
                        ) {
                            Text(statusOption.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Priority dropdown
            Text(
                text = "Priority",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedButton(
                onClick = { priorityDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(priority.name)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)

                DropdownMenu(
                    expanded = priorityDropdownExpanded,
                    onDismissRequest = { priorityDropdownExpanded = false }
                ) {
                    Priority.values().forEach { priorityOption ->
                        DropdownMenuItem(
                            onClick = {
                                priority = priorityOption
                                priorityDropdownExpanded = false
                            }
                        ) {
                            Text(priorityOption.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due date field
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // In a real app, you would create a Task object and save it
                    if (isCreating) {
                        onTaskCreated()
                    } else {
                        onTaskUpdated()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCreating) "Create" else "Update")
            }
        }
    }
}

@Preview
@Composable
fun TaskScreenPreview() {
    TaskItTheme {
        TaskCreatEditScreen(
            taskId = "1",
            isCreating = false
        )
    }
}

@Preview
@Composable
fun CreateTaskScreenPreview() {
    TaskItTheme {
        TaskCreatEditScreen(
            isCreating = true
        )
    }
}
