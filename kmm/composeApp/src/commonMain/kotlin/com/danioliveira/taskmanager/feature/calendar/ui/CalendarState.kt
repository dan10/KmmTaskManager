package com.danioliveira.taskmanager.feature.calendar.ui

import com.danioliveira.taskmanager.core.domain.model.Task
import kotlinx.datetime.LocalDate

/**
 * State for the Calendar screen.
 */
data class CalendarState(
    val selectedDate: LocalDate,
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int = 0
)

