package com.danioliveira.taskmanager.domain.usecase.tasks

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.TaskRepository

/**
 * Use case for updating task status.
 *
 * @property taskRepository The repository for task operations
 */
class UpdateTaskStatusUseCase(private val taskRepository: TaskRepository) {

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task to update
     * @param status The new status of the task
     * @return Result containing the updated task details
     */
    suspend operator fun invoke(taskId: String, status: TaskStatus): Result<TaskResponse> {
        return taskRepository.updateTaskStatus(taskId, status.name)
    }
} 