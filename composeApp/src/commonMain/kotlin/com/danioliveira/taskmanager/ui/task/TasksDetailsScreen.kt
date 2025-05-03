package com.danioliveira.taskmanager.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TasksDetailsScreen(
    taskId: String?,
    onBack: () -> Unit,
    onFilesClick: (String) -> Unit,
    onCommentsClick: (String) -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Task Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
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
                // Task title and basic info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Task ID: ${taskId ?: "Unknown"}",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Task details will be displayed here",
                            style = MaterialTheme.typography.body1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation options
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Task Resources",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Files button
                        Button(
                            onClick = { taskId?.let { onFilesClick(it) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Files")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Comments button
                        Button(
                            onClick = { taskId?.let { onCommentsClick(it) } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Comments")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TasksDetailsScreenPreview() {
    TaskItTheme {
        TasksDetailsScreen(
            taskId = "task-123",
            onBack = {},
            onFilesClick = {},
            onCommentsClick = {}
        )
    }
}
