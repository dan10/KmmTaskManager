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
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import com.danioliveira.taskmanager.core.ui.components.TaskItErrorState
import com.danioliveira.taskmanager.core.ui.components.TaskItLoadingState
import com.danioliveira.taskmanager.core.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementKey
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementType
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.add_24px
import kmmtaskmanager.composeapp.generated.resources.content_description_delete_task
import kmmtaskmanager.composeapp.generated.resources.content_description_edit_task
import kmmtaskmanager.composeapp.generated.resources.date_not_available
import kmmtaskmanager.composeapp.generated.resources.edit_24px
import kmmtaskmanager.composeapp.generated.resources.ic_calendar_month
import kmmtaskmanager.composeapp.generated.resources.ic_flag
import kmmtaskmanager.composeapp.generated.resources.task_created_field
import kmmtaskmanager.composeapp.generated.resources.task_dates_section
import kmmtaskmanager.composeapp.generated.resources.task_description_section
import kmmtaskmanager.composeapp.generated.resources.task_details_title
import kmmtaskmanager.composeapp.generated.resources.task_due_date_field
import kmmtaskmanager.composeapp.generated.resources.task_information_section
import kmmtaskmanager.composeapp.generated.resources.task_last_updated_field
import kmmtaskmanager.composeapp.generated.resources.task_no_details_error
import kmmtaskmanager.composeapp.generated.resources.task_no_due_date
import kmmtaskmanager.composeapp.generated.resources.task_priority_field
import kmmtaskmanager.composeapp.generated.resources.task_priority_suffix
import kmmtaskmanager.composeapp.generated.resources.task_status_field
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
                TaskDetailsTopBar(
                    isDeleting = state.isDeleting,
                    onAction = onAction
                )
            }
        ) { paddingValues ->
            TaskDetailsContent(
                state = state,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
private fun TaskDetailsTopBar(
    isDeleting: Boolean,
    onAction: (TasksDetailsAction) -> Unit
) {
    TaskItTopAppBar(
        title = stringResource(Res.string.task_details_title),
        showNavigationIcon = true,
        onNavigateBack = { onAction(TasksDetailsAction.NavigateBack) },
        actions = {
            TaskDetailsActions(
                isDeleting = isDeleting,
                onAction = onAction
            )
        }
    )
}

@Composable
private fun TaskDetailsActions(
    isDeleting: Boolean,
    onAction: (TasksDetailsAction) -> Unit
) {
    IconButton(onClick = { onAction(TasksDetailsAction.EditTask) }) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(Res.string.content_description_edit_task),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    
    IconButton(
        onClick = { onAction(TasksDetailsAction.DeleteTask) },
        enabled = !isDeleting
    ) {
        if (isDeleting) {
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

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun TaskDetailsContent(
    state: TasksDetailsState,
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
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
                    TaskDetailsBody(task = state.task)
                }
                else -> TaskItErrorState(stringResource(Res.string.task_no_details_error))
            }
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun TaskDetailsBody(task: com.danioliveira.taskmanager.core.domain.model.Task) {
    TaskHeaderCard(
        taskId = task.id,
        title = task.title,
        priority = task.priority,
        status = task.status
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (task.description.isNotBlank()) {
        DescriptionCard(
            taskId = task.id,
            description = task.description
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    TaskInformationCard(
        dueDate = task.dueDate,
        status = task.status,
        priority = task.priority
    )

    Spacer(modifier = Modifier.height(16.dp))

    DatesCard(
        createdAt = task.createdAt
    )
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
    val priorityColor = priority.toTaskPriority().color
    
    val extendedColors = TaskItThemeExt.colors
    
    with(sts) {
        Card(
            colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
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
                // Priority color left border
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(100.dp)
                        .background(priorityColor)
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
                        color = extendedColors.textPrimary,
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
                            tint = extendedColors.textSecondary
                        )

                        Text(
                            text = "${
                                priority.name.lowercase().replaceFirstChar { it.titlecase() }
                            } ${stringResource(Res.string.task_priority_suffix)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.textSecondary
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
    val extendedColors = TaskItThemeExt.colors
    
    with(sts) {
        Card(
            colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
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
                    text = stringResource(Res.string.task_description_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = extendedColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary,
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
    val extendedColors = TaskItThemeExt.colors
    
    Card(
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
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
                text = stringResource(Res.string.task_information_section),
                style = MaterialTheme.typography.titleMedium,
                color = extendedColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.ic_calendar_month),
                label = stringResource(Res.string.task_due_date_field),
                value = dueDate?.let { DateFormatter.formatDate(it) } ?: stringResource(Res.string.task_no_due_date)
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.edit_24px),
                label = stringResource(Res.string.task_status_field),
                value = TaskStatusFormatter.formatTaskStatus(status)
            )

            TaskInfoRow(
                icon = vectorResource(Res.drawable.ic_flag),
                label = stringResource(Res.string.task_priority_field),
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
    val extendedColors = TaskItThemeExt.colors
    
    with(sts) {
        val statusText = TaskStatusFormatter.formatTaskStatus(status)
        val backgroundColor = when (status) {
            TaskStatus.TODO -> extendedColors.statusTodoContainer
            TaskStatus.IN_PROGRESS -> extendedColors.statusInProgressContainer
            TaskStatus.DONE -> extendedColors.statusDoneContainer
        }
        val textColor = when (status) {
            TaskStatus.TODO -> extendedColors.statusTodoText
            TaskStatus.IN_PROGRESS -> extendedColors.statusInProgressText
            TaskStatus.DONE -> extendedColors.statusDoneText
        }
        
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "task_status_$taskId"),
                animatedVisibilityScope = avs
            )
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
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
    val extendedColors = TaskItThemeExt.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = extendedColors.iconNeutral
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.textSecondary,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DatesCard(createdAt: LocalDateTime?) {
    val extendedColors = TaskItThemeExt.colors
    
    Card(
        colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
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
                text = stringResource(Res.string.task_dates_section),
                style = MaterialTheme.typography.titleMedium,
                color = extendedColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            DateInfoRow(
                icon = vectorResource(Res.drawable.add_24px),
                label = stringResource(Res.string.task_created_field),
                value = createdAt?.let { DateFormatter.formatDate(it) } ?: stringResource(Res.string.date_not_available)
            )

            DateInfoRow(
                icon = Icons.Outlined.DateRange,
                label = stringResource(Res.string.task_last_updated_field),
                value = createdAt?.let { DateFormatter.formatDate(it) } ?: stringResource(Res.string.date_not_available)
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
    val extendedColors = TaskItThemeExt.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = extendedColors.iconPurple
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.textSecondary,
            modifier = Modifier.width(100.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = extendedColors.textPrimary,
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
