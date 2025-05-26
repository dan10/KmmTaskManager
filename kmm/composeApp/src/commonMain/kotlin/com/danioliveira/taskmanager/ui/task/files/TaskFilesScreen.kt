package com.danioliveira.taskmanager.ui.task.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.File
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.files_for_task
import kmmtaskmanager.composeapp.generated.resources.task_files_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TaskFilesScreen(
    viewModel: TaskFilesViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    viewModel.onBack = onBack

    Surface(color = Color(0XFFF1F5F9)) {
        TaskFilesScreenContent(
            state = viewModel.state,
            onAction = viewModel::handleActions
        )
    }
}

@Composable
private fun TaskFilesScreenContent(
    state: TaskFilesState,
    onAction: (TaskFilesAction) -> Unit
) {
    Scaffold(
        backgroundColor = Color(0XFFF1F5F9),
        topBar = {
            TaskFilesTopBar(onAction)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.files.isEmpty()) {
                Text(
                    text = "No files found for this task",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column {
                    if (state.taskId != null) {
                        Text(
                            text = stringResource(Res.string.files_for_task, state.taskId),
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    FilesList(files = state.files)
                }
            }
        }
    }
}

@Composable
private fun TaskFilesTopBar(onAction: (TaskFilesAction) -> Unit) {
    TopAppBar(
        title = { Text(stringResource(Res.string.task_files_title)) },
        navigationIcon = {
            IconButton(onClick = { onAction(TaskFilesAction.NavigateBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        }
    )
}

@Composable
private fun FilesList(files: List<File>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(files) { file ->
            FileItem(file)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FileItem(file: File) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${file.size} • Uploaded ${file.uploadedDate}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Preview
@Composable
fun TaskFilesScreenPreview() {
    TaskItTheme {
        TaskFilesScreenContent(
            state = TaskFilesState(
                isLoading = false,
                files = listOf(
                    File("document.pdf", "1.2 MB", "2023-01-01"),
                    File("image.jpg", "3.4 MB", "2023-01-02")
                ),
                taskId = "123"
            ),
            onAction = {}
        )
    }
}