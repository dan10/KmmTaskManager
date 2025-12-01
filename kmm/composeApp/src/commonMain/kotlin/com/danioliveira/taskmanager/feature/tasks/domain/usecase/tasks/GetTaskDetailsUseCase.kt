package com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks

import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository
import kotlin.uuid.Uuid

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
    suspend operator fun invoke(taskId: Uuid): Result<TaskResponse> {
        return taskRepository.getTask(taskId)
    }
}