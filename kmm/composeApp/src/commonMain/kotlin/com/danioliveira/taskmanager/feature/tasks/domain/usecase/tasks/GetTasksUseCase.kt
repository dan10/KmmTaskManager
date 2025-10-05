package com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks

import androidx.paging.PagingData
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository
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
