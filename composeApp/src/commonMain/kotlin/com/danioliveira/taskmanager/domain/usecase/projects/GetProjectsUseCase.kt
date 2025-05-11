package com.danioliveira.taskmanager.domain.usecase.projects

import androidx.paging.PagingData
import com.danioliveira.taskmanager.domain.Project
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching paginated projects.
 *
 * @property projectRepository The repository for project operations
 */
class GetProjectsUseCase(private val projectRepository: ProjectRepository) {

    /**
     * Gets a Flow of PagingData containing projects.
     *
     * @param pageSize The page size
     * @param query Optional query to filter projects by name
     * @return Flow of PagingData containing projects
     */
    operator fun invoke(pageSize: Int = 10, query: String?): Flow<PagingData<Project>> {
        return projectRepository.getProjectsStream(pageSize, query)
    }
}
