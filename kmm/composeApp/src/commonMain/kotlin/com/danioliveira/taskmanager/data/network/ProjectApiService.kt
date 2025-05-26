package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

/**
 * API service for project operations.
 */
class ProjectApiService(
    private val client: HttpClient
) {
    /**
     * Fetches paginated projects for the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @param query Optional query to filter projects by name
     * @return PaginatedResponse containing the projects
     */
    suspend fun getProjects(page: Int, size: Int, query: String? = null): PaginatedResponse<ProjectResponse> {
        return client.get("api/projects") {
            parameter("page", page)
            parameter("size", size)
            if (query != null && query.isNotBlank()) {
                parameter("query", query)
            }
        }.body()
    }

    /**
     * Fetches a specific project by ID.
     *
     * @param projectId The ID of the project
     * @return ProjectResponse containing the project details
     */
    suspend fun getProject(projectId: String): ProjectResponse {
        return client.get("api/projects/$projectId").body()
    }

    /**
     * Creates a new project.
     *
     * @param request The project creation request
     * @return ProjectResponse containing the created project details
     */
    suspend fun createProject(request: ProjectCreateRequest): ProjectResponse {
        return client.post("api/projects") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Updates an existing project.
     *
     * @param projectId The ID of the project to update
     * @param request The project update request
     * @return True if the project was updated successfully, false otherwise
     */
    suspend fun updateProject(projectId: String, request: ProjectUpdateRequest): Boolean {
        val response = client.put("api/projects/$projectId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status == HttpStatusCode.OK
    }

    /**
     * Deletes a project.
     *
     * @param projectId The ID of the project to delete
     * @return True if the project was deleted successfully, false otherwise
     */
    suspend fun deleteProject(projectId: String): Boolean {
        val response = client.delete("api/projects/$projectId")
        return response.status == HttpStatusCode.NoContent
    }
}
