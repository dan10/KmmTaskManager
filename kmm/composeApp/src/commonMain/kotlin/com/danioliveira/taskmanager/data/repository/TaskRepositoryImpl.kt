package com.danioliveira.taskmanager.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.network.TaskApiService
import com.danioliveira.taskmanager.data.paging.ProjectTaskPagingSource
import com.danioliveira.taskmanager.data.paging.TaskPagingSource
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of TaskRepository that uses TaskApiService.
 *
 * @property apiService The API service for task operations
 */
class TaskRepositoryImpl(
    private val apiService: TaskApiService
) : TaskRepository {

    override fun getTasksStream(pageSize: Int, query: String?): Flow<PagingData<Task>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TaskPagingSource(apiService, query) }
        ).flow
    }

    override fun getProjectTasksStream(projectId: String, pageSize: Int): Flow<PagingData<Task>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProjectTaskPagingSource(apiService, projectId) }
        ).flow
    }

    override suspend fun getTasks(page: Int, size: Int, query: String?): Result<PaginatedResponse<TaskResponse>> {
        return try {
            val response = apiService.getTasks(page, size, query)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch tasks: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getTask(taskId: String): Result<TaskResponse> {
        return try {
            val response = apiService.getTask(taskId)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch task: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: String): Result<TaskResponse> {
        return try {
            val response = apiService.updateTaskStatus(taskId, status)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to update task status: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getTaskProgress(query: String?): Result<TaskProgressResponse> {
        return try {
            val response = apiService.getTaskProgress(query)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch task progress: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun createTask(request: TaskCreateRequest): Result<TaskResponse> {
        return try {
            val response = apiService.createTask(request)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to create task: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun updateTask(taskId: String, request: TaskUpdateRequest): Result<TaskResponse> {
        return try {
            val response = apiService.updateTask(taskId, request)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to update task: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Boolean> {
        return try {
            val success = apiService.deleteTask(taskId)
            Result.success(success)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to delete task: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getTasksByProjectId(
        projectId: String,
        page: Int,
        size: Int
    ): Result<PaginatedResponse<TaskResponse>> {
        return try {
            val response = apiService.getTasksByProjectId(projectId, page, size)
            Result.success(response)
        } catch (e: ClientRequestException) {
            // Handle client errors (4xx)
            Result.failure(Exception("Failed to fetch tasks for project: ${e.message}"))
        } catch (e: ServerResponseException) {
            // Handle server errors (5xx)
            Result.failure(Exception("Server error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("Unknown error: ${e.message}"))
        }
    }
}
