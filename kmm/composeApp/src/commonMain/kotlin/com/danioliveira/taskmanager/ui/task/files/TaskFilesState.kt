package com.danioliveira.taskmanager.ui.task.files

import com.danioliveira.taskmanager.domain.File

data class TaskFilesState(
    val isLoading: Boolean = true,
    val files: List<File> = emptyList(),
    val errorMessage: String? = null,
    val taskId: String? = null
)

sealed interface TaskFilesAction {
    data object LoadFiles : TaskFilesAction
    data object NavigateBack : TaskFilesAction
}