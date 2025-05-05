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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.TaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinNavViewModel

@Composable
fun TasksDetailsScreen(
    viewModel: TasksDetailsViewModel = koinNavViewModel(),
    onBack: () -> Unit,
    onFilesClick: (String) -> Unit,
    onCommentsClick: (String) -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
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
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { onAction(TasksDetailsAction.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
                    }
                }
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
                TaskInfoCard(
                    title = "Urgent Meeting",
                    priority = TaskPriority.HIGH,
                    description = "Prepare presentation for client meeting. Need to review all quarterly metrics and create executive summary.",
                    dueDate = "2024-11-25",
                    project = "Website Redesign",
                    assignedTo = "John Doe"
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                CommentsSection(
                    comments = listOf(
                        Comment("Sarah Smith", "2 hours ago", "I've shared the Q3 metrics in the drive"),
                        Comment("Mike Johnson", "5 hours ago", "Let's review the deck tomorrow morning")
                    ),
                    onViewAll = {
                        // Navigate to all comments
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                FilesSection(
                    files = listOf(
                        File("Q4_Presentation.pdf", "2.4 MB", "2024-11-20"),
                        File("Metrics_Summary.xlsx", "1.1 MB", "2024-11-19")
                    ),
                    onViewAll = {
                        // Navigate to all files
                    }
                )
            }
        }
    }
}

@Composable
fun TaskInfoCard(
    title: String,
    priority: TaskPriority,
    description: String,
    dueDate: String,
    project: String,
    assignedTo: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
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
        InfoRow(label = "Due:", value = dueDate)
        InfoRow(label = "Project:", value = project)
        InfoRow(label = "Assigned to:", value = assignedTo)
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
fun CommentsSection(
    comments: List<Comment>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Comments", onViewAll = onViewAll)
        Spacer(modifier = Modifier.height(8.dp))
        comments.forEach { comment ->
            CommentItem(comment)
        }
        Spacer(modifier = Modifier.height(8.dp))
        AddComment()
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(text = comment.userName, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.padding(2.dp))
            Text(text = comment.time, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.padding(2.dp))
            Text(text = comment.commentText, style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
fun AddComment() {
    var commentText by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Add a comment...") }
        )
        IconButton(onClick = { /*TODO: Handle send comment*/ }) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
        }
    }
}

@Composable
fun FilesSection(
    files: List<File>,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        SectionHeader(title = "Files", onViewAll = onViewAll)
        Spacer(modifier = Modifier.height(8.dp))
        files.forEach { file ->
            FileItem(file)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add file", tint = MaterialTheme.colors.primary)
            Spacer(modifier = Modifier.padding(2.dp))
            Text(text = "Add files", style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.primary)
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
            contentDescription = "File",
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
            text = "View all",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.primary
        )
    }
}

data class Comment(val userName: String, val time: String, val commentText: String)
data class File(val name: String, val size: String, val uploadedDate: String)

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
