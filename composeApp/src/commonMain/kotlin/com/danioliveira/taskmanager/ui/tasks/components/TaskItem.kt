package com.danioliveira.taskmanager.ui.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.ui.tasks.TasksAction
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TaskItem(
    task: Task,
    onAction: (TasksAction) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAction(TasksAction.OpenTask(task)) },
        elevation = 2.dp,
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
                        color = if (task.isDone)
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
            }

            Checkbox(
                checked = task.isDone,
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

@Preview
@Composable
fun TaskItemPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High Priority Task
            TaskItem(
                task = Task(
                    id = 1,
                    title = "Urgent Meeting",
                    description = "Prepare presentation for client meeting",
                    dueDate = "2024-11-25",
                    priority = Priority.HIGH
                ),
                onAction = {},
                onCheckedChange = {}
            )

            // Medium Priority Task
            TaskItem(
                task = Task(
                    id = 2,
                    title = "Review Code",
                    description = "Review pull requests for feature branch",
                    dueDate = "2024-11-26",
                    priority = Priority.MEDIUM
                ),
                onAction = {},
                onCheckedChange = {}
            )

            // Low Priority Task
            TaskItem(
                task = Task(
                    id = 3,
                    title = "Update Documentation",
                    description = "Update project wiki with new features",
                    dueDate = "2024-11-30",
                    priority = Priority.LOW,
                    isDone = true
                ),
                onAction = {},
                onCheckedChange = {}
            )
        }
    }
}