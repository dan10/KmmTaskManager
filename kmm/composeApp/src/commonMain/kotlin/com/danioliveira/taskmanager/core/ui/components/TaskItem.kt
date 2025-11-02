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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.danioliveira.taskmanager.core.domain.model.TaskPriority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementKey
import com.danioliveira.taskmanager.feature.tasks.ui.TaskSharedElementType
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.task_chip_due_date
import kmmtaskmanager.composeapp.generated.resources.task_chip_due_date_overdue
import kmmtaskmanager.composeapp.generated.resources.task_chip_priority
import kmmtaskmanager.composeapp.generated.resources.task_chip_project
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.Uuid

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showProjectName: Boolean = true
) {
    val state = rememberTaskItemState(task)

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
            colors = CardDefaults.cardColors(containerColor = state.colors.container)
        ) {
            TaskItemContent(
                state = state,
                showProjectName = showProjectName,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskItemContent(
    state: TaskItemState,
    showProjectName: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        TaskIndicatorBar(taskId = state.task.id, color = state.colors.indicator)

        TaskMainContent(
            state = state,
            showProjectName = showProjectName,
            modifier = Modifier.weight(1f)
        )

        TaskItemCheckbox(
            state = state,
            onCheckedChange = onCheckedChange
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskIndicatorBar(taskId: Uuid, color: Color) {
    with(sts) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(color)
                .sharedElement(
                    rememberSharedContentState(key = "task_indicator_$taskId"),
                    animatedVisibilityScope = avs
                )
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskMainContent(
    state: TaskItemState,
    showProjectName: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskTitleAndStatus(state)

        if (state.task.description.isNotBlank()) {
            TaskDescription(state)
        }

        TaskMetaInfo(state, showProjectName)
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskTitleAndStatus(state: TaskItemState) {
    with(sts) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = state.task.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = state.colors.title,
                textDecoration = if (state.task.status == TaskStatus.DONE) {
                    TextDecoration.LineThrough
                } else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .sharedElement(
                        rememberSharedContentState(key = "task_title_${state.task.id}"),
                        animatedVisibilityScope = avs
                    )
            )

            TaskStatusBadge(state)
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskStatusBadge(state: TaskItemState) {
    with(sts) {
        Surface(
            color = state.colors.statusBackground,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "task_status_${state.task.id}"),
                animatedVisibilityScope = avs
            )
        ) {
            Text(
                text = TaskStatusFormatter.formatTaskStatus(state.task.status),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = state.colors.statusText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TaskDescription(state: TaskItemState) {
    with(sts) {
        Text(
            text = state.task.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            ),
            color = state.colors.description,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "task_description_${state.task.id}"),
                animatedVisibilityScope = avs
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskMetaInfo(
    state: TaskItemState,
    showProjectName: Boolean
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.task.dueDate?.let { dueDate ->
            TaskDueDateChip(dueDate, state.isOverdue)
        }

        TaskPriorityChip(state.task.priority, state.priority)

        state.task.projectName?.takeIf { showProjectName && it.isNotBlank() }?.let {
            TaskProjectChip(it)
        }
    }
}

@Composable
private fun TaskDueDateChip(dueDate: LocalDateTime, isOverdue: Boolean) {
    val extendedColors = TaskItThemeExt.colors
    val dueDateLabel = DateFormatter.formatDate(dueDate)
    val chipLabel = if (isOverdue) {
        stringResource(Res.string.task_chip_due_date_overdue, dueDateLabel)
    } else {
        stringResource(Res.string.task_chip_due_date, dueDateLabel)
    }

    TaskInfoChip(
        label = chipLabel,
        containerColor = if (isOverdue) extendedColors.chipDueDateOverdueContainer else extendedColors.chipDueDateContainer,
        contentColor = if (isOverdue) extendedColors.chipDueDateOverdueText else extendedColors.chipDueDateText
    )
}

@Composable
private fun TaskPriorityChip(priority: Priority, taskPriority: TaskPriority) {
    val priorityColors = taskPriority.getColors()
    TaskInfoChip(
        label = stringResource(
            Res.string.task_chip_priority,
            PriorityFormatter.formatPriority(priority)
        ),
        containerColor = priorityColors.container,
        contentColor = priorityColors.text
    )
}

@Composable
private fun TaskProjectChip(projectName: String) {
    val extendedColors = TaskItThemeExt.colors
    TaskInfoChip(
        label = stringResource(Res.string.task_chip_project, projectName),
        containerColor = extendedColors.chipProjectContainer,
        contentColor = extendedColors.chipProjectText
    )
}

@Composable
private fun TaskItemCheckbox(
    state: TaskItemState,
    onCheckedChange: (Boolean) -> Unit
) {
    val extendedColors = TaskItThemeExt.colors
    Checkbox(
        checked = state.task.status == TaskStatus.DONE,
        onCheckedChange = onCheckedChange,
        colors = CheckboxDefaults.colors(
            checkedColor = state.colors.indicator,
            uncheckedColor = extendedColors.checkboxUnchecked,
            checkmarkColor = extendedColors.checkboxCheckmark
        ),
        modifier = Modifier.padding(end = 8.dp, top = 12.dp)
    )
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
