package com.danioliveira.taskmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.toTaskPriority
import com.danioliveira.taskmanager.utils.PriorityFormatter

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
        style = MaterialTheme.typography.caption,
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
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(taskPriority.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = PriorityFormatter.formatPriority(priority),
            style = MaterialTheme.typography.caption,
            color = taskPriority.color
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
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2
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
        elevation = 2.dp
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
            style = MaterialTheme.typography.h6,
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
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption,
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
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 12.dp)
    )
} 