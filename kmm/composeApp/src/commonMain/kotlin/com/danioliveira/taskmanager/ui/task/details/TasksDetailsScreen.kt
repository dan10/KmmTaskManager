package com.danioliveira.taskmanager.ui.task.details

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.ui.components.TaskItEditDeleteButtons
import com.danioliveira.taskmanager.ui.components.TaskItErrorState
import com.danioliveira.taskmanager.ui.components.TaskItHeaderWithPriority
import com.danioliveira.taskmanager.ui.components.TaskItInfoCard
import com.danioliveira.taskmanager.ui.components.TaskItInfoRow
import com.danioliveira.taskmanager.ui.components.TaskItLoadingState
import com.danioliveira.taskmanager.ui.components.TaskItSectionTitle
import com.danioliveira.taskmanager.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.task_actions
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kmmtaskmanager.composeapp.generated.resources.task_due_date
import kmmtaskmanager.composeapp.generated.resources.task_no_due_date
import kmmtaskmanager.composeapp.generated.resources.task_project
import kmmtaskmanager.composeapp.generated.resources.task_status_label
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksDetailsScreen(
    viewModel: TasksDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onEditTask: (String) -> Unit = {}
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
        topBar = {
            TaskItTopAppBar(
                title = stringResource(Res.string.task_details_title),
                onNavigateBack = { onAction(TasksDetailsAction.NavigateBack) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                when {
                    state.isLoading -> TaskItLoadingState()
                    state.errorMessage != null -> TaskItErrorState(state.errorMessage)
                    state.task != null -> {
                        TaskInfoCard(
                            title = state.task.title,
                            priority = state.task.priority,
                            description = state.task.description,
                            status = state.task.status,
                            dueDate = state.task.dueDate,
                            projectName = state.task.projectName
                        )
                    }
                    else -> TaskItErrorState("No task details available")
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
}

@Composable
fun TaskActionButtons(
    isDeleting: Boolean,
    onAction: (TasksDetailsAction) -> Unit
) {
    TaskItInfoCard {
        TaskItSectionTitle(stringResource(Res.string.task_actions))
        
        TaskItEditDeleteButtons(
            onEdit = { onAction(TasksDetailsAction.EditTask) },
            onDelete = { onAction(TasksDetailsAction.DeleteTask) },
            isDeleting = isDeleting,
            deleteEnabled = !isDeleting
        )
    }
}

@Composable
fun TaskInfoCard(
    title: String,
    priority: Priority,
    description: String,
    status: TaskStatus,
    dueDate: LocalDateTime?,
    projectName: String?
) {
    TaskItInfoCard {
        // Header with title and priority
        TaskItHeaderWithPriority(
            title = title,
            priority = priority
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Task information rows
        TaskItInfoRow(
            label = stringResource(Res.string.task_due_date),
            value = if (dueDate != null) {
                DateFormatter.formatDate(dueDate)
            } else {
                stringResource(Res.string.task_no_due_date)
            }
        )
        
        TaskItInfoRow(
            label = stringResource(Res.string.task_status_label),
            value = TaskStatusFormatter.formatTaskStatus(status)
        )
        
        // Project row - only show if task has a project
        projectName?.let { project ->
            TaskItInfoRow(
                label = stringResource(Res.string.task_project),
                value = project
            )
        }
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
