package com.danioliveira.taskmanager.core.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.util.HapticFeedbackType
import com.danioliveira.taskmanager.util.rememberHapticFeedback
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalLayoutApi::class)
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
        isOverdue -> Color(0xFFFFF1F2)
        task.status == TaskStatus.DONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    val titleColor = if (task.status == TaskStatus.DONE) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val descriptionColor = if (task.status == TaskStatus.DONE) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val indicatorBrush = Brush.verticalGradient(
        colors = listOf(
            indicatorColor.copy(alpha = 0.95f),
            indicatorColor.copy(alpha = 0.65f)
        )
    )

    val statusColor = when (task.status) {
        TaskStatus.TODO -> Color(0xFF6B7280)
        TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
        TaskStatus.DONE -> Color(0xFF10B981)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 14.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(indicatorBrush)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 72.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = titleColor,
                        textDecoration = if (task.status == TaskStatus.DONE) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = TaskStatusFormatter.formatTaskStatus(task.status),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = descriptionColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    task.dueDate?.let { dueDate ->
                        val dueDateLabel = DateFormatter.formatDate(dueDate)
                        TaskMetaChip(
                            label = "Due • " + dueDateLabel + if (isOverdue) " • Overdue" else "",
                            containerColor = if (isOverdue) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            contentColor = if (isOverdue) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TaskMetaChip(
                        label = "Priority • " + PriorityFormatter.formatPriority(task.priority),
                        containerColor = priority.backgroundColor,
                        contentColor = priority.color
                    )

                    val projectName = task.projectName
                    if (showProjectName && !projectName.isNullOrBlank()) {
                        TaskMetaChip(
                            label = "Project • " + projectName,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

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
                    uncheckedColor = indicatorColor.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TaskMetaChip(
    label: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun TaskItemPreview() {
    TaskItTheme {
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
