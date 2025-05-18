package com.danioliveira.taskmanager.domain.usecase.projects

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.domain.repository.ProjectRepository

/**
 * Use case for creating and editing projects.
 *
 * @property projectRepository The repository for project operations
 */
class CreateEditProjectUseCase(private val projectRepository: ProjectRepository) {

    /**
     * Gets a specific project by ID.
     *
     * @param projectId The ID of the project
     * @return Result containing the project details
     */
    suspend fun getProject(projectId: String): Result<ProjectResponse> {
        return projectRepository.getProject(projectId)
    }

    /**
     * Creates a new project.
     *
     * @param name The project name
     * @param description The project description (optional)
     * @return Result containing the created project details
     */
    suspend fun createProject(
        name: String,
        description: String?
    ): Result<ProjectResponse> {
        val request = ProjectCreateRequest(
            name = name,
            description = description
        )

        return projectRepository.createProject(request)
    }

    /**
     * Updates an existing project.
     *
     * @param projectId The ID of the project to update
     * @param name The project name
     * @param description The project description (optional)
     * @return Result containing true if the project was updated successfully, false otherwise
     */
    suspend fun updateProject(
        projectId: String,
        name: String,
        description: String?
    ): Result<Boolean> {
        val request = ProjectUpdateRequest(
            name = name,
            description = description
        )

        return projectRepository.updateProject(projectId, request)
    }
}