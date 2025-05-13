package com.danioliveira.taskmanager.domain.usecase.projects

import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.domain.repository.ProjectRepository

/**
 * Use case for getting project details.
 *
 * @property projectRepository The repository for project operations
 */
class GetProjectDetailsUseCase(private val projectRepository: ProjectRepository) {
    /**
     * Gets a specific project by ID.
     *
     * @param projectId The ID of the project
     * @return Result containing the project details
     */
    suspend operator fun invoke(projectId: String): Result<ProjectResponse> {
        return projectRepository.getProject(projectId)
    }
}