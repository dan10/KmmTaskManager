package com.danioliveira.taskmanager.domain.usecase.tasks

import com.danioliveira.taskmanager.domain.repository.TaskRepository

/**
 * Use case for deleting tasks.
 *
 * @property taskRepository The repository for task operations
 */
class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    /**
     * Deletes a task.
     *
     * @param taskId The ID of the task to delete
     * @return Result containing true if the task was deleted successfully, false otherwise
     */
    suspend operator fun invoke(taskId: String): Result<Boolean> {
        return taskRepository.deleteTask(taskId)
    }
} 