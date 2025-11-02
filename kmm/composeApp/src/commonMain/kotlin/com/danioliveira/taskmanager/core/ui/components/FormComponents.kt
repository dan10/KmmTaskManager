package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.toTaskPriority
import com.danioliveira.taskmanager.utils.PriorityFormatter
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable field label for forms.
 */
@Composable
fun TaskItFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

/**
 * Priority badge component for displaying task priority.
 */
@Composable
fun TaskItPriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val taskPriority = priority.toTaskPriority()
    val priorityColors = taskPriority.getColors()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(priorityColors.container)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = PriorityFormatter.formatPriority(priority),
            style = MaterialTheme.typography.labelSmall,
            color = priorityColors.text
        )
    }
}

/**
 * Information row displaying label and value.
 */
@Composable
fun TaskItInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Information card for displaying structured data.
 */
@Composable
fun TaskItInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

/**
 * Header text with priority badge layout.
 */
@Composable
fun TaskItHeaderWithPriority(
    title: String,
    priority: Priority,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        TaskItPriorityBadge(priority = priority)
    }
}

/**
 * Error message display for forms.
 */
@Composable
fun TaskItErrorMessage(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    errorMessage?.let { error ->
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            modifier = modifier.padding(bottom = 16.dp)
        )
    }
}

/**
 * Section title for organizing form content.
 */
@Composable
fun TaskItSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 12.dp)
    )
}


@Preview
@Composable
private fun TaskItFieldLabelPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskItFieldLabel(text = "Email Address")
            TaskItFieldLabel(text = "Password")
            TaskItFieldLabel(text = "Description (Optional)")
        }
    }
}

@Preview
@Composable
private fun TaskItPriorityBadgePreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Priority Badges",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("None:")
                TaskItPriorityBadge(priority = Priority.NONE)
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Low:")
                TaskItPriorityBadge(priority = Priority.LOW)
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Medium:")
                TaskItPriorityBadge(priority = Priority.MEDIUM)
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("High:")
                TaskItPriorityBadge(priority = Priority.HIGH)
            }
        }
    }
}

@Preview
@Composable
private fun TaskItInfoRowPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskItInfoRow(
                label = "Status",
                value = "In Progress"
            )
            TaskItInfoRow(
                label = "Due Date",
                value = "Nov 15, 2025"
            )
            TaskItInfoRow(
                label = "Assigned To",
                value = "John Doe"
            )
            TaskItInfoRow(
                label = "Priority",
                value = "High"
            )
        }
    }
}

@Preview
@Composable
private fun TaskItInfoCardPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskItInfoCard {
                Text(
                    text = "Task Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TaskItInfoRow(label = "Status", value = "In Progress")
                TaskItInfoRow(label = "Priority", value = "High")
                TaskItInfoRow(label = "Due Date", value = "Nov 15, 2025")
            }
            
            TaskItInfoCard {
                Text(
                    text = "Project Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TaskItInfoRow(label = "Total Tasks", value = "24")
                TaskItInfoRow(label = "Completed", value = "18")
                TaskItInfoRow(label = "In Progress", value = "6")
            }
        }
    }
}

@Preview
@Composable
private fun TaskItHeaderWithPriorityPreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskItHeaderWithPriority(
                title = "Complete Project Documentation",
                priority = Priority.HIGH
            )
            
            TaskItHeaderWithPriority(
                title = "Review Pull Requests",
                priority = Priority.MEDIUM
            )
            
            TaskItHeaderWithPriority(
                title = "Update Dependencies",
                priority = Priority.LOW
            )
        }
    }
}

@Preview
@Composable
private fun TaskItErrorMessagePreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Error Messages",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            TaskItErrorMessage(
                errorMessage = "Please enter a valid email address"
            )
            
            TaskItErrorMessage(
                errorMessage = "Password must be at least 8 characters long"
            )
            
            TaskItErrorMessage(
                errorMessage = "This field is required"
            )
            
            TaskItErrorMessage(
                errorMessage = null
            )
            
            Text(
                text = "(null message shows nothing)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun TaskItSectionTitlePreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskItSectionTitle(title = "Task Details")
            Text("Section content goes here...")
            
            TaskItSectionTitle(title = "Additional Information")
            Text("More section content...")
            
            TaskItSectionTitle(title = "Attachments")
            Text("Files and documents...")
        }
    }
}

@Preview
@Composable
private fun TaskItFormComponentsShowcasePreview() {
    TaskItTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskItSectionTitle(title = "Form Components Showcase")
            
            TaskItHeaderWithPriority(
                title = "Task Title with Priority",
                priority = Priority.HIGH
            )
            
            TaskItInfoCard {
                TaskItFieldLabel(text = "Task Information")
                TaskItInfoRow(label = "Status", value = "In Progress")
                TaskItInfoRow(label = "Due Date", value = "Nov 15, 2025")
            }
            
            TaskItErrorMessage(
                errorMessage = "Example error message"
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskItPriorityBadge(priority = Priority.HIGH)
                TaskItPriorityBadge(priority = Priority.MEDIUM)
                TaskItPriorityBadge(priority = Priority.LOW)
            }
        }
    }
}
 