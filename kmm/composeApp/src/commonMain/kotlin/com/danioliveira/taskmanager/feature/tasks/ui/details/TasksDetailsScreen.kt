package com.danioliveira.taskmanager.feature.tasks.ui.details

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementKey
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementType
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.add_24px
import kmmtaskmanager.composeapp.generated.resources.content_description_delete_task
import kmmtaskmanager.composeapp.generated.resources.content_description_edit_task
import kmmtaskmanager.composeapp.generated.resources.edit_24px
import kmmtaskmanager.composeapp.generated.resources.ic_calendar_month
import kmmtaskmanager.composeapp.generated.resources.ic_flag
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

context(_: SharedTransitionScope, _: AnimatedVisibilityScope)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TaskDetailsScreen(
    viewModel: TasksDetailsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onEditTask: (Uuid) -> Unit
) {
    viewModel.onBack = onBack
    viewModel.onEditTask = onEditTask

    TasksDetailsScreenContent(
        state = viewModel.state,
        onAction = viewModel::handleActions
    )
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TasksDetailsScreenContent(
    state: TasksDetailsState,
    onAction: (TasksDetailsAction) -> Unit
) {
    with(sts) {
        Scaffold(
            modifier = Modifier.sharedBounds(
                sts.rememberSharedContentState(
                    TaskSharedElementKey(
                        state.task?.id.toString(),
                        TaskSharedElementType.Bounds
                    )
                ), avs
            ),
            containerColor = Color(0XFFF5F5F5),
            topBar = {
                TaskItTopAppBar(
                    title = stringResource(Res.string.task_details_title),
                    showNavigationIcon = true,
                    onNavigateBack = { onAction(TasksDetailsAction.NavigateBack) },
                    actions = {
                        IconButton(onClick = { onAction(TasksDetailsAction.EditTask) }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(Res.string.content_description_edit_task),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { onAction(TasksDetailsAction.DeleteTask) },
                            enabled = !state.isDeleting
                        ) {
                            if (state.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(Res.string.content_description_delete_task),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when {
                        state.isLoading -> TaskItLoadingState()
                        state.errorMessage != null -> TaskItErrorState(state.errorMessage)
                        state.task != null -> {
                            TaskHeaderCard(
                                taskId = state.task.id,
                                title = state.task.title,
                                priority = state.task.priority,
                                status = state.task.status
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.task.description.isNotBlank()) {
                                DescriptionCard(
                                    taskId = state.task.id,
                                    description = state.task.description
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            TaskInformationCard(
                                dueDate = state.task.dueDate,
                                status = state.task.status,
                                priority = state.task.priority
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DatesCard(
                                createdAt = state.task.createdAt
                            )
                        }

                        else -> TaskItErrorState("No task details available")
                    }
                }
            }
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskHeaderCard(
    taskId: Uuid,
    title: String,
    priority: Priority,
    status: TaskStatus
) {
    with(sts) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "task_card_$taskId"),
                animatedVisibilityScope = avs
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Yellow left border
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(100.dp)
                        .background(Color(0xFFFDB022))
                        .sharedElement(
                            rememberSharedContentState(key = "task_indicator_$taskId"),
                            animatedVisibilityScope = avs
                        )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "task_title_$taskId"),
                            animatedVisibilityScope = avs
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            taskId = taskId,
                            status = status
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF6B7280)
                        )

                        Text(
                            text = "${
                                priority.name.lowercase().replaceFirstChar { it.titlecase() }
                            } Priority",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DescriptionCard(
    taskId: Uuid,
    description: String
) {
    with(sts) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    lineHeight = 22.sp,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "task_description_$taskId"),
                        animatedVisibilityScope = avs
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskInformationCard(
    dueDate: LocalDateTime?,
    status: TaskStatus,
    priority: Priority
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Task Information",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.ic_calendar_month),
                label = "Due Date",
                value = dueDate?.let { DateFormatter.formatDate(it) } ?: "No due date"
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.edit_24px),
                label = "Status",
                value = TaskStatusFormatter.formatTaskStatus(status)
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.ic_flag),
                label = "Priority",
                value = priority.name.lowercase().replaceFirstChar { it.titlecase() }
            )
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun StatusBadge(
    taskId: Uuid,
    status: TaskStatus
) {
    with(sts) {
        val statusText = TaskStatusFormatter.formatTaskStatus(status)
        Surface(
            color = Color(0xFFE8F5E9),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "task_status_$taskId"),
                animatedVisibilityScope = avs
            )
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TaskInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF9CA3AF)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DatesCard(createdAt: LocalDateTime?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dates",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Bold
            )

            DateInfoRow(
                icon = vectorResource(Res.drawable.add_24px),
                label = "Created",
                value = createdAt?.let { DateFormatter.formatDate(it) } ?: "—"
            )

            DateInfoRow(
                icon = Icons.Outlined.DateRange,
                label = "Last Updated",
                value = createdAt?.let { DateFormatter.formatDate(it) } ?: "—"
            )
        }
    }
}

@Composable
private fun DateInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF7C3AED)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1A1A1A),
            fontWeight = FontWeight.Medium
        )
    }
}

//@Preview
//@Composable
//fun TasksDetailsScreenPreview() {
//    TaskItTheme {
//        TasksDetailsScreenContent(
//            state = TasksDetailsState(),
//            onAction = {}
//        )
//    }
//}
