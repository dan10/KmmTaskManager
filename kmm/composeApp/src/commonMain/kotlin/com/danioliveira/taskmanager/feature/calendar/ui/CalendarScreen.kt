package com.danioliveira.taskmanager.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.ui.components.PrincipalTaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.components.TaskList
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_calendar_month
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun CalendarScreen(
    onGlobalSearch: (String) -> Unit = {},
    onTaskClick: (String) -> Unit = {}
) {
    val viewModel = koinViewModel<CalendarViewModel>()
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CalendarEffect.NavigateToTaskDetail -> onTaskClick(effect.taskId)
            }
        }
    }

    Scaffold(
        topBar = {
            CalendarTopBar(onGlobalSearch = onGlobalSearch)
        }
    ) { paddingValues ->
        CalendarContent(
            state = state,
            onAction = viewModel::onAction,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun CalendarTopBar(onGlobalSearch: (String) -> Unit) {
    with(sts) {
        PrincipalTaskItTopAppBar(
            title = "Calendar",
            onSearch = onGlobalSearch,
            modifier = Modifier.sharedBounds(
                sts.rememberSharedContentState(key = "main_top_bar"),
                avs
            )
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun CalendarContent(
    state: CalendarState,
    onAction: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 7-day rolling strip
        WeekDayStrip(
            selectedDate = state.selectedDate,
            onDateSelected = { onAction(CalendarAction.SelectDate(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header with date and count
        TasksHeader(
            date = state.selectedDate,
            count = state.totalCount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content: loading, error, or list with empty state
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(state.error)
            else -> TaskList(
                tasks = state.tasks,
                enableSwipe = true,
                onTaskClick = { task ->
                    onAction(CalendarAction.TaskClicked(task.id.toString()))
                },
                onTaskCheckedChange = { taskId, checked ->
                    onAction(CalendarAction.TaskCheckedChanged(taskId, checked))
                },
                onTaskSwipeComplete = { task ->
                    val checked = task.status == com.danioliveira.taskmanager.core.domain.model.TaskStatus.DONE
                    onAction(CalendarAction.TaskCheckedChanged(task.id.toString(), !checked))
                },
                onTaskSwipeDelete = { task ->
                    // TODO: Add delete action to CalendarAction
                },
                emptyContent = { EmptyContent() }
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun WeekDayStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    val startDate = selectedDate.minus(3, DateTimeUnit.DAY)
    val dates = (0..6).map { startDate.plus(it, DateTimeUnit.DAY) }

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dates.forEach { date ->
            DayItem(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayOfWeek = dayNames[date.dayOfWeek.ordinal]
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun TasksHeader(
    date: LocalDate,
    count: Int,
    modifier: Modifier = Modifier
) {
    // TODO create a list in strings.xml in en, es, pt
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val formattedDate = "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tasks for $formattedDate",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$count tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(error: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_calendar_month),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks scheduled for this date",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
