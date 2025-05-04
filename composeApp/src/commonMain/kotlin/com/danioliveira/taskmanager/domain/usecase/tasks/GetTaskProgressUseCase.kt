package com.danioliveira.taskmanager.domain.usecase.tasks

import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.domain.repository.TaskRepository

/**
 * Use case for fetching the user's task progress.
 *
 * @property taskRepository The repository for task operations
 */
class GetTaskProgressUseCase(private val taskRepository: TaskRepository) {

    /**
     * Executes the get task progress use case.
     *
     * @return Result containing the task progress on success or an exception on failure
     */
    suspend operator fun invoke(): Result<TaskProgressResponse> {
        // Use the dedicated getTaskProgress method that calls the /tasks/progress endpoint
        return taskRepository.getTaskProgress()
    }
}
