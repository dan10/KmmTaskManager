package com.danioliveira.taskmanager.feature.tasks.domain.usecase.calendar

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository

/**
 * Use case for fetching tasks assigned to the current user due on a specific date.
 */
class GetTasksDueOnUseCase(
    private val repository: TaskRepository
) {
    /**
     * Fetches tasks due on the specified date.
     *
     * @param date The date in YYYY-MM-DD format
     * @param page The page number (0-based)
     * @param size The page size
     * @return Result containing paginated tasks due on the specified date
     */
    suspend operator fun invoke(
        date: String,
        page: Int = 0,
        size: Int = 50
    ): Result<PaginatedResponse<TaskResponse>> {
        return repository.getAssignedTasksDueOn(date, page, size)
    }
}



