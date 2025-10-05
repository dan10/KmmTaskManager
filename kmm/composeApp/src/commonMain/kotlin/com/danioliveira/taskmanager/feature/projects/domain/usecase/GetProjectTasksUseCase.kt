package com.danioliveira.taskmanager.feature.projects.domain.usecase

import androidx.paging.PagingData
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting tasks for a specific project.
 *
 * @property taskRepository The repository for task operations
 */
class GetProjectTasksUseCase(private val taskRepository: TaskRepository) {
    /**
     * Gets a Flow of PagingData containing tasks for a specific project.
     *
     * @param projectId The ID of the project
     * @param pageSize The page size
     * @return Flow of PagingData containing tasks for the project
     */
    operator fun invoke(projectId: String, pageSize: Int = 10): Flow<PagingData<Task>> {
        return taskRepository.getProjectTasksStream(projectId, pageSize)
    }
}
