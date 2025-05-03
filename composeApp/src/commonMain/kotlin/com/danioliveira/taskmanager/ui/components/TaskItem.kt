package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        elevation = 1.dp,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (task.status == TaskStatus.DONE)
                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colors.onSurface
                    )

                    // Priority Tag
                    Surface(
                        color = priority.backgroundColor,
                        shape = MaterialTheme.shapes.small,
                        elevation = 0.dp
                    ) {
                        Text(
                            text = task.priority.name,
                            style = MaterialTheme.typography.caption.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = priority.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )

                task.projectName?.let {
                    Text(
                        text = "Project: $it",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
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
                    dueDate = "2024-11-25",
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
                    dueDate = "2024-11-26",
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
                    dueDate = "2024-11-30",
                    projectName = null
                ),
                onClick = {},
                onCheckedChange = {}
            )
        }
    }
}
