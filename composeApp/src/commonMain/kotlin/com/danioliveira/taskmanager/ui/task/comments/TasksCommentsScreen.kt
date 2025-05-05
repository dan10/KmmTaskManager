package com.danioliveira.taskmanager.ui.task.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class Comment(
    val author: String,
    val text: String,
    val timestamp: String
)

@Composable
fun TasksCommentsScreen(
    taskId: String?,
    onBack: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    // Sample comments
    val comments = remember {
        listOf(
            Comment("John Doe", "This task needs more clarification.", "2 hours ago"),
            Comment("Jane Smith", "I've started working on this.", "Yesterday"),
            Comment("Mike Johnson", "The deadline seems tight.", "2 days ago"),
            Comment("Sarah Williams", "I can help with this task if needed.", "3 days ago")
        )
    }

    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Task Comments") },
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
                Text(
                    text = "Comments for Task: ${taskId ?: "Unknown"}",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Comments list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add comment section
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add a comment") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Add comment logic */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Post Comment")
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = comment.timestamp,
                    style = MaterialTheme.typography.caption
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider()
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Preview
@Composable
fun TasksCommentsScreenPreview() {
    TaskItTheme {
        TasksCommentsScreen(
            taskId = "task-123",
            onBack = {}
        )
    }
}