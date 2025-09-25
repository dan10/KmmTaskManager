package com.danioliveira.taskmanager.domain.usecase.tasks

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import kotlinx.datetime.LocalDateTime

/**
 * Use case for creating and editing tasks.
 *
 * @property taskRepository The repository for task operations
 */
class CreateEditTaskUseCase(private val taskRepository: TaskRepository) {

    /**
     * Creates a new task.
     *
     * @param title The task title
     * @param description The task description (optional)
     * @param priority The task priority
     * @param dueDate The task due date (optional)
     * @param projectId The project ID to associate the task with (optional)
     * @param assigneeId The user ID to assign the task to (optional)
     * @return Result containing the created task details
     */
    suspend fun createTask(
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDateTime?,
        projectId: String? = null,
        assigneeId: String? = null
    ): Result<TaskResponse> {
        val request = TaskCreateRequest(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            projectId = projectId,
            assigneeId = assigneeId
        )

        return taskRepository.createTask(request)
    }

    /**
     * Updates an existing task.
     *
     * @param taskId The ID of the task to update
     * @param title The task title
     * @param description The task description (optional)
     * @param priority The task priority
     * @param dueDate The task due date (optional)
     * @param status The task status
     * @return Result containing the updated task details
     */
    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        priority: Priority,
        dueDate: LocalDateTime?,
        status: TaskStatus
    ): Result<TaskResponse> {
        val request = TaskUpdateRequest(
            title = title,
            description = description,
            status = status,
            priority = priority,
            dueDate = dueDate
        )

        return taskRepository.updateTask(taskId, request)
    }

    /**
     * Deletes a task.
     *
     * @param taskId The ID of the task to delete
     * @return Result containing true if the task was deleted successfully, false otherwise
     */
    suspend fun deleteTask(taskId: String): Result<Boolean> {
        return taskRepository.deleteTask(taskId)
    }

    /**
     * Gets a specific task by ID.
     *
     * @param taskId The ID of the task
     * @return Result containing the task details
     */
    suspend fun getTask(taskId: String): Result<TaskResponse> {
        return taskRepository.getTask(taskId)
    }
}
