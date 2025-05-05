package com.danioliveira.taskmanager.ui.task.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TasksDetailsViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var state by mutableStateOf(TasksDetailsState())
        private set

    var onBack: () -> Unit = {}
    var onFilesClick: (String) -> Unit = {}
    var onCommentsClick: (String) -> Unit = {}

    init {
        loadTaskDetails()
    }

    private fun loadTaskDetails() {
        val taskId = savedStateHandle.get<String>("taskId")
        viewModelScope.launch {
            // If needed, convert String to Uuid for API calls
            // val uuid = taskId?.let { Uuid.fromString(it) }
        }
    }

    fun handleActions(action: TasksDetailsAction) {
        when (action) {
            is TasksDetailsAction.LoadTaskDetails -> loadTaskDetails()
            is TasksDetailsAction.NavigateToFiles -> navigateToFiles(action.taskId)
            is TasksDetailsAction.NavigateToComments -> navigateToComments(action.taskId)
            is TasksDetailsAction.NavigateBack -> navigateBack()
        }
    }

    private fun navigateToFiles(taskId: String) {
        onFilesClick(taskId)
    }

    private fun navigateToComments(taskId: String) {
        onCommentsClick(taskId)
    }

    private fun navigateBack() {
        onBack()
    }
}
