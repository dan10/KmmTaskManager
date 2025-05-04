package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for task operations.
 */
class TaskApiService(
    private val client: HttpClient
) {
    /**
     * Fetches paginated tasks for the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @param query Optional query to filter tasks by title
     * @return PaginatedResponse containing the tasks and task progress information
     */
    suspend fun getTasks(page: Int, size: Int, query: String? = null): PaginatedResponse<TaskResponse> {
        return client.get("/tasks") {
            parameter("page", page)
            parameter("size", size)
            if (query != null && query.isNotBlank()) {
                parameter("query", query)
            }
        }.body()
    }

    /**
     * Fetches a specific task by ID.
     *
     * @param taskId The ID of the task
     * @return TaskResponse containing the task details
     */
    suspend fun getTask(taskId: String): TaskResponse {
        return client.get("/tasks/$taskId").body()
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return TaskResponse containing the updated task details
     */
    suspend fun updateTaskStatus(taskId: String, status: String): TaskResponse {
        return client.post("/tasks/$taskId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body()
    }

    /**
     * Fetches task progress for the current user.
     *
     * @param query Optional query to filter tasks by title
     * @return TaskProgressResponse containing the task progress information
     */
    suspend fun getTaskProgress(query: String? = null): TaskProgressResponse {
        return client.get("/tasks/progress") {
            if (query != null && query.isNotBlank()) {
                parameter("query", query)
            }
        }.body()
    }

    /**
     * Creates a new task.
     *
     * @param request The task creation request
     * @return TaskResponse containing the created task details
     */
    suspend fun createTask(request: TaskCreateRequest): TaskResponse {
        return client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Updates an existing task.
     *
     * @param taskId The ID of the task to update
     * @param request The task update request
     * @return TaskResponse containing the updated task details
     */
    suspend fun updateTask(taskId: String, request: TaskUpdateRequest): TaskResponse {
        return client.put("/tasks/$taskId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Deletes a task.
     *
     * @param taskId The ID of the task to delete
     * @return True if the task was deleted successfully, false otherwise
     */
    suspend fun deleteTask(taskId: String): Boolean {
        val response = client.delete("/tasks/$taskId")
        return response.status == HttpStatusCode.NoContent
    }
}
