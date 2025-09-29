package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.DateFormatter
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.utils.TaskStatusFormatter
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.task_due_date
import kmmtaskmanager.composeapp.generated.resources.task_project
import kmmtaskmanager.composeapp.generated.resources.task_status_label
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showProjectName: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val priority = task.priority.toTaskPriority()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.status == TaskStatus.DONE)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = priority.backgroundColor,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = PriorityFormatter.formatPriority(task.priority),
                            style = MaterialTheme.typography.labelMedium,
                            color = priority.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                task.dueDate?.let {
                    Text(
                        text = "${stringResource(Res.string.task_due_date)} ${DateFormatter.formatDate(it)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${stringResource(Res.string.task_status_label)}: ${TaskStatusFormatter.formatTaskStatus(task.status)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (showProjectName && task.projectName != null) {
                    Text(
                        text = "${stringResource(Res.string.task_project)} ${task.projectName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Checkbox(
                checked = task.status == TaskStatus.DONE,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = priority.color,
                    uncheckedColor = priority.color.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(start = 8.dp)
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
                    projectName = "Website Redesign"
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
                    projectName = "Website Redesign"
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
                    projectName = null
                ),
                onClick = {},
                onCheckedChange = {}
            )
        }
    }
}
