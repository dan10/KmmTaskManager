package com.danioliveira.taskmanager.domain.usecase.tasks

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.repository.TaskRepository

/**
 * Use case for getting task details.
 *
 * @property taskRepository The repository for task operations
 */
class GetTaskDetailsUseCase(private val taskRepository: TaskRepository) {
    /**
     * Gets a specific task by ID.
     *
     * @param taskId The ID of the task
     * @return Result containing the task details
     */
    suspend operator fun invoke(taskId: String): Result<TaskResponse> {
        return taskRepository.getTask(taskId)
    }
}