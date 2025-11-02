package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementKey
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementType
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.util.HapticFeedbackType
import com.danioliveira.taskmanager.util.rememberHapticFeedback
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.task_chip_due_date
import kmmtaskmanager.composeapp.generated.resources.task_chip_due_date_overdue
import kmmtaskmanager.composeapp.generated.resources.task_chip_priority
import kmmtaskmanager.composeapp.generated.resources.task_chip_project
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalTime::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showProjectName: Boolean = true
) {
    val priority = task.priority.toTaskPriority()
    val haptic = rememberHapticFeedback()

    // Check if task is overdue
    val isOverdue = task.dueDate?.let { dueDate ->
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        task.status != TaskStatus.DONE && dueDate < now
    } ?: false

    val indicatorColor = when {
        isOverdue -> Color(0xFFEF4444)
        else -> priority.color
    }

    val containerColor = when {
        isOverdue -> Color(0xFFFFF5F5)
        task.status == TaskStatus.DONE -> Color(0xFFF9FAFB)
        else -> Color.White
    }

    val titleColor = if (task.status == TaskStatus.DONE) {
        Color(0xFF9CA3AF)
    } else {
        Color(0xFF1A1A1A)
    }

    val descriptionColor = if (task.status == TaskStatus.DONE) {
        Color(0xFFD1D5DB)
    } else {
        Color(0xFF6B7280)
    }

    val statusColor = when (task.status) {
        TaskStatus.TODO -> Color(0xFF6B7280)
        TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        TaskStatus.DONE -> Color(0xFF10B981)
    }

    val statusBackgroundColor = when (task.status) {
        TaskStatus.TODO -> Color(0xFFF3F4F6)
        TaskStatus.IN_PROGRESS -> Color(0xFFDCEEFE)
        TaskStatus.DONE -> Color(0xFFD1FAE5)
    }

    with(sts) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .sharedBounds(
                    sts.rememberSharedContentState(
                        TaskSharedElementKey(
                            task.id.toString(),
                            TaskSharedElementType.Bounds
                        )
                    ), avs
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // Left indicator bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(indicatorColor)
                        .sharedElement(
                            rememberSharedContentState(key = "task_indicator_${task.id}"),
                            animatedVisibilityScope = avs
                        )
                )

                // Main content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title and Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = titleColor,
                            textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .sharedElement(
                                    rememberSharedContentState(key = "task_title_${task.id}"),
                                    animatedVisibilityScope = avs
                                )
                        )

                        Surface(
                            color = statusBackgroundColor,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "task_status_${task.id}"),
                                animatedVisibilityScope = avs
                            )
                        ) {
                            Text(
                                text = TaskStatusFormatter.formatTaskStatus(task.status),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Description
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = descriptionColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "task_description_${task.id}"),
                                animatedVisibilityScope = avs
                            )
                        )
                    }

                    // Meta information
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Due Date
                        task.dueDate?.let { dueDate ->
                            val dueDateLabel = DateFormatter.formatDate(dueDate)
                            val chipLabel = if (isOverdue) {
                                stringResource(Res.string.task_chip_due_date_overdue, dueDateLabel)
                            } else {
                                stringResource(Res.string.task_chip_due_date, dueDateLabel)
                            }
                            TaskInfoChip(
                                label = chipLabel,
                                containerColor = if (isOverdue) Color(0xFFFEE2E2) else Color(0xFFF3F4F6),
                                contentColor = if (isOverdue) Color(0xFFDC2626) else Color(0xFF4B5563)
                            )
                        }

                        // Priority
                        TaskInfoChip(
                            label = stringResource(
                                Res.string.task_chip_priority,
                                PriorityFormatter.formatPriority(task.priority)
                            ),
                            containerColor = priority.backgroundColor,
                            contentColor = priority.color
                        )

                        // Project
                        val projectName = task.projectName
                        if (showProjectName && !projectName.isNullOrBlank()) {
                            TaskInfoChip(
                                label = stringResource(Res.string.task_chip_project, projectName),
                                containerColor = Color(0xFFEDE9FE),
                                contentColor = Color(0xFF7C3AED)
                            )
                        }
                    }
                }

                // Checkbox
                Checkbox(
                    checked = task.status == TaskStatus.DONE,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            haptic.performHapticFeedback(HapticFeedbackType.Success)
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                        }
                        onCheckedChange(isChecked)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = indicatorColor,
                        uncheckedColor = Color(0xFFD1D5DB),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 8.dp, top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskInfoChip(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    TaskItTheme {
        SharedTransitionScope {
            AnimatedVisibility(true) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskItem(
                        task = Task(
                            id = Uuid.random(),
                            title = "Urgent Meeting",
                            description = "Prepare presentation for client meeting",
                            status = TaskStatus.TODO,
                            priority = Priority.HIGH,
                            dueDate = LocalDateTime.parse("2024-11-25T00:00:00"),
                            projectName = "Website Redesign",
                            createdAt = LocalDateTime.parse("2024-11-20T10:00:00")
                        ),
                        onClick = {},
                        onCheckedChange = {}
                    )

                    TaskItem(
                        task = Task(
                            id = Uuid.random(),
                            title = "Review Code",
                            description = "Review pull requests for feature branch",
                            status = TaskStatus.IN_PROGRESS,
                            priority = Priority.MEDIUM,
                            dueDate = LocalDateTime.parse("2024-11-26T00:00:00"),
                            projectName = "Website Redesign",
                            createdAt = LocalDateTime.parse("2024-11-20T10:00:00")
                        ),
                        onClick = {},
                        onCheckedChange = {}
                    )

                    TaskItem(
                        task = Task(
                            id = Uuid.random(),
                            title = "Update Documentation",
                            description = "Update project wiki with new features",
                            status = TaskStatus.DONE,
                            priority = Priority.LOW,
                            dueDate = LocalDateTime.parse("2024-11-30T00:00:00"),
                            projectName = null,
                            createdAt = LocalDateTime.parse("2024-11-20T10:00:00")
                        ),
                        onClick = {},
                        onCheckedChange = {}
                    )
                }
            }
        }
    }
}
