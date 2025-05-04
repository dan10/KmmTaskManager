package com.danioliveira.taskmanager.domain.usecase.tasks

import app.cash.paging.PagingData
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching paginated tasks.
 *
 * @property taskRepository The repository for task operations
 */
class GetTasksUseCase(private val taskRepository: TaskRepository) {

    /**
     * Gets a Flow of PagingData containing tasks.
     *
     * @param pageSize The page size
     * @param query Optional query to filter tasks by title
     * @return Flow of PagingData containing tasks
     */
    operator fun invoke(pageSize: Int = 10, query: String?): Flow<PagingData<Task>> {
        return taskRepository.getTasksStream(pageSize, query)
    }

}
