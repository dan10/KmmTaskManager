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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.File
import com.danioliveira.taskmanager.domain.TaskPriority
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.content_description_file
import kmmtaskmanager.composeapp.generated.resources.content_description_more
import kmmtaskmanager.composeapp.generated.resources.task_add_files
import kmmtaskmanager.composeapp.generated.resources.task_assigned_to
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kmmtaskmanager.composeapp.generated.resources.task_due_date
import kmmtaskmanager.composeapp.generated.resources.task_files
import kmmtaskmanager.composeapp.generated.resources.task_project
import kmmtaskmanager.composeapp.generated.resources.task_view_all
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksDetailsScreen(
    viewModel: TasksDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onFilesClick: (String) -> Unit
) {
    viewModel.onBack = onBack
    viewModel.onFilesClick = onFilesClick

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
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(Res.string.content_description_more)
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
                    project = state.task.projectName ?: "No Project",
                    assignedTo = "Assigned User" // This would need to be fetched from the user repository
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

        item {
            FilesSection(
                files = listOf(
                    File("Q4_Presentation.pdf", "2.4 MB", "2024-11-20"),
                    File("Metrics_Summary.xlsx", "1.1 MB", "2024-11-19"),
                    File("Budget_2024.xlsx", "1.3 MB", "2024-11-18"),
                    File("Client_Feedback.docx", "0.8 MB", "2024-11-17")
                ),
                onViewAll = {
                    state.task?.id?.toString()?.let { taskId ->
                        onAction(TasksDetailsAction.NavigateToFiles(taskId))
                    }
                }
            )
        }
    }
}

@Composable
fun TaskInfoCard(
    title: String,
    priority: TaskPriority,
    description: String,
    dueDate: LocalDateTime?,
    project: String,
    assignedTo: String
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
            InfoRow(label = stringResource(Res.string.task_due_date), value = dueDate?.toString() ?: "No due date")
            InfoRow(label = stringResource(Res.string.task_project), value = project)
            InfoRow(label = stringResource(Res.string.task_assigned_to), value = assignedTo)
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
            text = priority.name,
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


@Composable
fun FilesSection(
    files: List<File>,
    onViewAll: () -> Unit
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
            SectionHeader(title = stringResource(Res.string.task_files), onViewAll = onViewAll)
            Spacer(modifier = Modifier.height(8.dp))
            if (files.isEmpty()) {
                Text(
                    text = "No files available",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                files.take(3).forEach { file ->
                    FileItem(file)
                }
                if (files.size > 3) {
                    Text(
                        text = "... and ${files.size - 3} more files",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.task_add_files),
                    tint = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = stringResource(Res.string.task_add_files),
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun FileItem(file: File) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(Res.string.content_description_file),
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(text = file.name, style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.padding(2.dp))
            Text(text = "${file.size} • Uploaded ${file.uploadedDate}", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
        Text(
            modifier = Modifier.clickable { onViewAll() },
            text = stringResource(Res.string.task_view_all),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary
        )
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
