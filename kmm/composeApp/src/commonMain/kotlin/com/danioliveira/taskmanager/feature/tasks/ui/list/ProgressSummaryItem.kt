package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_completed
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_percentage
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
private fun ProgressHeader(completedTasks: Int, totalTasks: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.tasks_progress_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = if (totalTasks == 0) "No tasks yet" else stringResource(
                    Res.string.tasks_progress_completed,
                    completedTasks,
                    totalTasks
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shape = CircleShape
        ) {
            Text(
                text = "$completedTasks/$totalTasks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ProgressBar(animatedProgress: Float, gradient: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                .background(gradient)
        )
    }
}

@Composable
private fun ProgressFooter(totalTasks: Int, targetProgress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (totalTasks == 0) "Welcome! Let's add your first task." else "You're making steady progress",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(
                Res.string.tasks_progress_percentage,
                (targetProgress * 100).roundToInt()
            ),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProgressSummaryItem(
    completedTasks: Int,
    totalTasks: Int,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600)
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProgressHeader(completedTasks, totalTasks)
            ProgressBar(animatedProgress, gradient)
            ProgressFooter(totalTasks, targetProgress)
        }
    }
}

@Preview
@Composable
private fun ProgressSummaryItemPreview() {
    TaskItTheme {
        ProgressSummaryItem(
            completedTasks = 5,
            totalTasks = 12,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun ProgressSummaryItemNoTasksPreview() {
    TaskItTheme {
        ProgressSummaryItem(
            completedTasks = 0,
            totalTasks = 0,
            modifier = Modifier.padding(16.dp)
        )
    }
}