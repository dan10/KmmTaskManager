package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.danioliveira.taskmanager.domain.Task

class TasksViewModel: ViewModel() {

     var state by mutableStateOf(TasksState())
         private set

    init {
        loadTasks()
    }

    private fun loadTasks() {
        // Load tasks from repository
    }

    private fun createTask() {
        // Create task from repository
    }

    private fun updateTask(task: Task) {
        // Update task from repository
    }

    private fun deleteTask(task: Task) {
        // Delete task from repository
    }

    private fun openTask(task: Task) {
        // Open task from repository
    }


    fun handleActions(action: TasksAction) {
        when(action) {
            is TasksAction.LoadTasks -> loadTasks()
            is TasksAction.DeleteTask -> deleteTask(action.task)
            is TasksAction.UpdateTask -> updateTask(action.task)
            is TasksAction.CreateTask -> createTask()
            is TasksAction.OpenTask -> openTask(action.task)
        }
    }
}