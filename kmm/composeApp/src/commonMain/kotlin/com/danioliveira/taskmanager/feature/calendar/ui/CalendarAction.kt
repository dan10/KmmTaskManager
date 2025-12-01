package com.danioliveira.taskmanager.feature.calendar.ui

import kotlinx.datetime.LocalDate

/**
 * Actions for the Calendar screen.
 */
sealed interface CalendarAction {
    data class SelectDate(val date: LocalDate) : CalendarAction
    data class TaskClicked(val taskId: String) : CalendarAction
    data class TaskCheckedChanged(val taskId: String, val checked: Boolean) : CalendarAction
    data object Refresh : CalendarAction
}



