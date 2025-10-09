package com.danioliveira.taskmanager.feature.tasks.ui.details

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.TaskItErrorState
import com.danioliveira.taskmanager.core.ui.components.TaskItLoadingState
import com.danioliveira.taskmanager.core.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.theme.TaskDetailColors
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_delete_task
import kmmtaskmanager.composeapp.generated.resources.content_description_edit_task
import kmmtaskmanager.composeapp.generated.resources.ic_calendar_month
import kmmtaskmanager.composeapp.generated.resources.schedule_24px
import kmmtaskmanager.composeapp.generated.resources.task_created_label
import kmmtaskmanager.composeapp.generated.resources.task_description_label
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kmmtaskmanager.composeapp.generated.resources.task_due_date
import kmmtaskmanager.composeapp.generated.resources.task_no_due_date
import kmmtaskmanager.composeapp.generated.resources.task_project
import kmmtaskmanager.composeapp.generated.resources.work_24px
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    viewModel: TasksDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onEditTask: (Uuid) -> Unit
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
        containerColor = Color(0XFFF1F5F9),
        topBar = {
            TaskItTopAppBar(
                title = stringResource(Res.string.task_details_title),
                showNavigationIcon = true,
                onNavigateBack = { onAction(TasksDetailsAction.NavigateBack) },
                actions = {
                    // Placeholder for actions, will be handled in layout below
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                when {
                    state.isLoading -> TaskItLoadingState()
                    state.errorMessage != null -> TaskItErrorState(state.errorMessage)
                    state.task != null -> TaskDetailsCard(
                        title = state.task.title,
                        priority = state.task.priority,
                        status = state.task.status,
                        description = state.task.description,
                        dueDate = state.task.dueDate,
                        createdAt = state.task.createdAt,
                        projectName = state.task.projectName,
                        isDeleting = state.isDeleting,
                        onEdit = { onAction(TasksDetailsAction.EditTask) },
                        onDelete = { onAction(TasksDetailsAction.DeleteTask) }
                    )
                    else -> TaskItErrorState("No task details available")
                }
            }
        }
    }
}

@Composable
private fun TaskDetailsCard(
    title: String,
    priority: Priority,
    status: TaskStatus,
    description: String,
    dueDate: LocalDateTime?,
    createdAt: LocalDateTime?,
    projectName: String?,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.dp.value.toInt().sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagChip(
                        label = TaskStatusFormatter.formatTaskStatus(status),
                        background = TaskDetailColors.PillSecondary,
                        contentColor = TaskDetailColors.PillSecondaryContent
                    )
                    TagChip(
                        label = priority.name.lowercase().replaceFirstChar { it.titlecase() },
                        background = TaskDetailColors.PillPrimary,
                        contentColor = TaskDetailColors.PillPrimaryContent
                    )
                }
            }

            // Description Section
            if (description.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(text = stringResource(Res.string.task_description_label))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF4A4A4A),
                        lineHeight = 24.dp.value.toInt().sp
                    )
                }
            }

            // Metadata Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(
                    icon = vectorResource(Res.drawable.ic_calendar_month),
                    title = stringResource(Res.string.task_due_date),
                    value = dueDate?.let { DateFormatter.formatDate(it) }
                        ?: stringResource(Res.string.task_no_due_date)
                )

                InfoRow(
                    icon = vectorResource(Res.drawable.schedule_24px),
                    title = stringResource(Res.string.task_created_label),
                    value = createdAt?.let { DateFormatter.formatDate(it) } ?: "—"
                )

                projectName?.let { project ->
                    InfoRow(
                        icon = vectorResource(Res.drawable.work_24px),
                        title = stringResource(Res.string.task_project),
                        value = project
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Filled.Edit,
                    label = "Edit",
                    contentDescription = stringResource(Res.string.content_description_edit_task),
                    backgroundColor = TaskDetailColors.MetaIconColor.copy(alpha = 0.12f),
                    contentColor = TaskDetailColors.MetaIconColor,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    icon = Icons.Filled.Delete,
                    label = "Delete",
                    contentDescription = stringResource(Res.string.content_description_delete_task),
                    backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = onDelete,
                    isLoading = isDeleting,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    background: Color,
    contentColor: Color
) {
    val animatedColor by animateColorAsState(background)
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        color = animatedColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF6B7280),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F3FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF6750A4),
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
