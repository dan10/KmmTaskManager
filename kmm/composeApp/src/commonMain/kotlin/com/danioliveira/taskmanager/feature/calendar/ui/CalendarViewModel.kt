package com.danioliveira.taskmanager.feature.calendar.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.feature.tasks.data.mapper.toTask
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.calendar.GetTasksDueOnUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.UpdateTaskStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

/**
 * ViewModel for the Calendar screen.
 */
@OptIn(ExperimentalUuidApi::class)
class CalendarViewModel(
    private val getTasksDueOnUseCase: GetTasksDueOnUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase
) : ViewModel() {

    @OptIn(ExperimentalTime::class)
    var state by mutableStateOf(
        CalendarState(
            selectedDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        )
    )
        private set

    private val _effects = MutableSharedFlow<CalendarEffect>()
    val effects: SharedFlow<CalendarEffect> = _effects.asSharedFlow()

    init {
        loadTasksForSelectedDate()
    }

    fun onAction(action: CalendarAction) {
        when (action) {
            is CalendarAction.SelectDate -> selectDate(action.date)
            is CalendarAction.TaskClicked -> navigateToTaskDetail(action.taskId)
            is CalendarAction.TaskCheckedChanged -> updateTaskStatus(action.taskId, action.checked)
            CalendarAction.Refresh -> loadTasksForSelectedDate()
        }
    }

    private fun selectDate(date: LocalDate) {
        state = state.copy(selectedDate = date)
        loadTasksForSelectedDate()
    }

    private fun loadTasksForSelectedDate() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val dateString = state.selectedDate.toString() // Format: YYYY-MM-DD
            
            getTasksDueOnUseCase(date = dateString, page = 0, size = 50)
                .onSuccess { response ->
                    state = state.copy(
                        tasks = response.items.map { it.toTask() },
                        totalCount = response.total,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    private fun navigateToTaskDetail(taskId: String) {
        viewModelScope.launch {
            _effects.emit(CalendarEffect.NavigateToTaskDetail(taskId))
        }
    }

    private fun updateTaskStatus(taskId: String, checked: Boolean) {
        viewModelScope.launch {
            val status = if (checked) TaskStatus.DONE else TaskStatus.TODO
            updateTaskStatusUseCase(taskId, status)
                .onSuccess {
                    // Refresh the list after status update
                    loadTasksForSelectedDate()
                }
                .onFailure { error ->
                    state = state.copy(error = error.message)
                }
        }
    }
}

/**
 * Effects for the Calendar screen.
 */
sealed interface CalendarEffect {
    data class NavigateToTaskDetail(val taskId: String) : CalendarEffect
}



