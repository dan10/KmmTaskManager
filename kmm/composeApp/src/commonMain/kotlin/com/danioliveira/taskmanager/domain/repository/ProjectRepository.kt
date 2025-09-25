package com.danioliveira.taskmanager.domain.repository

import androidx.paging.PagingData
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.domain.Project
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for project operations.
 */
interface ProjectRepository {
    /**
     * Gets paginated projects for the current user using Paging3.
     *
     * @param pageSize The page size
     * @param query Optional query to filter projects by name
     * @return Flow of PagingData containing projects
     */
    fun getProjectsStream(pageSize: Int, query: String?): Flow<PagingData<Project>>

    /**
     * Gets paginated projects for the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @param query Optional query to filter projects by name
     * @return Result containing paginated projects
     */
    suspend fun getProjects(page: Int, size: Int, query: String? = null): Result<PaginatedResponse<ProjectResponse>>

    /**
     * Gets a specific project by ID.
     *
     * @param projectId The ID of the project
     * @return Result containing the project details
     */
    suspend fun getProject(projectId: String): Result<ProjectResponse>

    /**
     * Creates a new project.
     *
     * @param request The project creation request
     * @return Result containing the created project details
     */
    suspend fun createProject(request: ProjectCreateRequest): Result<ProjectResponse>

    /**
     * Updates an existing project.
     *
     * @param projectId The ID of the project to update
     * @param request The project update request
     * @return Result containing true if the project was updated successfully, false otherwise
     */
    suspend fun updateProject(projectId: String, request: ProjectUpdateRequest): Result<Boolean>

    /**
     * Deletes a project.
     *
     * @param projectId The ID of the project to delete
     * @return Result containing true if the project was deleted successfully, false otherwise
     */
    suspend fun deleteProject(projectId: String): Result<Boolean>
}
