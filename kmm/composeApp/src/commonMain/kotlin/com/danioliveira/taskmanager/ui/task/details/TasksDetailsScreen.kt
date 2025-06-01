package com.danioliveira.taskmanager.ui.task.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskPriority
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.util.DateFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.content_description_edit_task
import kmmtaskmanager.composeapp.generated.resources.content_description_delete_task
import kmmtaskmanager.composeapp.generated.resources.task_actions
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kmmtaskmanager.composeapp.generated.resources.task_due_date
import kmmtaskmanager.composeapp.generated.resources.task_edit_button
import kmmtaskmanager.composeapp.generated.resources.task_delete_button
import kmmtaskmanager.composeapp.generated.resources.task_no_due_date
import kmmtaskmanager.composeapp.generated.resources.task_project
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksDetailsScreen(
    viewModel: TasksDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onEditTask: (String) -> Unit = {},
    onDeleteTask: (String) -> Unit = {}
) {
    viewModel.onBack = onBack
    viewModel.onEditTask = onEditTask

    Surface(color = Color(0XFFF1F5F9)) {
        TasksDetailsScreenContent(
            state = viewModel.state,
            onAction = viewModel::handleActions
        )
    }
}

@Composable
private fun TasksDetailsScreenContent(
    state: TasksDetailsState,
    onAction: (TasksDetailsAction) -> Unit
) {
    Scaffold(
        backgroundColor = Color(0XFFF1F5F9),
        topBar = {
            TaskDetailsTopBar(onAction)
        }
    ) { paddingValues ->
        TaskDetailsContent(state, paddingValues, onAction)
    }
}

@Composable
private fun TaskDetailsTopBar(onAction: (TasksDetailsAction) -> Unit) {
    TopAppBar(
        title = { Text(stringResource(Res.string.task_details_title)) },
        navigationIcon = {
            IconButton(onClick = { onAction(TasksDetailsAction.NavigateBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        }
    )
}

@Composable
private fun TaskDetailsContent(
    state: TasksDetailsState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    onAction: (TasksDetailsAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        item {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.error
                    )
                }
            } else if (state.task != null) {
                TaskInfoCard(
                    title = state.task.title,
                    priority = state.task.priority.toTaskPriority(),
                    description = state.task.description,
                    dueDate = state.task.dueDate,
                    projectName = state.task.projectName
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No task details available",
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action buttons section
        item {
            if (state.task != null) {
                TaskActionButtons(
                    isDeleting = state.isDeleting,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
fun TaskActionButtons(
    isDeleting: Boolean,
    onAction: (TasksDetailsAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.task_actions),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onAction(TasksDetailsAction.EditTask) },
                    modifier = Modifier.weight(1f),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(Res.string.content_description_edit_task),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(Res.string.task_edit_button))
                }
                
                OutlinedButton(
                    onClick = { onAction(TasksDetailsAction.DeleteTask) },
                    modifier = Modifier.weight(1f),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colors.error,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(Res.string.content_description_delete_task),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(Res.string.task_delete_button))
                }
            }
        }
    }
}

@Composable
fun TaskInfoCard(
    title: String,
    priority: TaskPriority,
    description: String,
    dueDate: LocalDateTime?,
    projectName: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )
                PriorityBadge(priority = priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.body2)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Due date row
            InfoRow(
                label = stringResource(Res.string.task_due_date), 
                value = if (dueDate != null) {
                    DateFormatter.formatDate(dueDate)
                } else {
                    stringResource(Res.string.task_no_due_date)
                }
            )
            
            // Project row - only show if task has a project
            projectName?.let { project ->
                InfoRow(
                    label = stringResource(Res.string.task_project), 
                    value = project
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(priority.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = when (priority) {
                TaskPriority.HIGH -> PriorityFormatter.formatPriority(Priority.HIGH)
                TaskPriority.MEDIUM -> PriorityFormatter.formatPriority(Priority.MEDIUM)
                TaskPriority.LOW -> PriorityFormatter.formatPriority(Priority.LOW)
            },
            style = MaterialTheme.typography.caption,
            color = priority.color
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(2.dp))
        Text(text = value, style = MaterialTheme.typography.body2)
    }
}

@Preview
@Composable
fun TasksDetailsScreenPreview() {
    TaskItTheme {
        TasksDetailsScreenContent(
            state = TasksDetailsState(),
            onAction = {}
        )
    }
}
