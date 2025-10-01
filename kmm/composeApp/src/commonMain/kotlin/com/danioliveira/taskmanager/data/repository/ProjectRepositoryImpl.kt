package com.danioliveira.taskmanager.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.network.ProjectApiService
import com.danioliveira.taskmanager.data.paging.ProjectPagingSource
import com.danioliveira.taskmanager.domain.Project
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of ProjectRepository that uses ProjectApiService.
 *
 * @property apiService The API service for project operations
 */
class ProjectRepositoryImpl(
    private val apiService: ProjectApiService
) : ProjectRepository {

    override fun getProjectsStream(pageSize: Int, query: String?): Flow<PagingData<Project>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProjectPagingSource(apiService, query) }
        ).flow
    }

    override suspend fun getProjects(
        page: Int,
        size: Int,
        query: String?
    ): Result<PaginatedResponse<ProjectResponse>> {
        return try {
            val response = apiService.getProjects(page, size, query)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch projects: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getProject(projectId: String): Result<ProjectResponse> {
        return try {
            val response = apiService.getProject(projectId)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch project: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun createProject(request: ProjectCreateRequest): Result<ProjectResponse> {
        return try {
            val response = apiService.createProject(request)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to create project: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun updateProject(
        projectId: String,
        request: ProjectUpdateRequest
    ): Result<Boolean> {
        return try {
            val success = apiService.updateProject(projectId, request)
            Result.success(success)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to update project: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Boolean> {
        return try {
            val success = apiService.deleteProject(projectId)
            Result.success(success)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to delete project: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }
}
