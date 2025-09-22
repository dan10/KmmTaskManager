package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.TaskAssignRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskStatusChangeRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.api.routes.Tasks
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

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
        val resource = Tasks.Assigned(
            size = size,
            page = page,
            query = query
        )
        return client.get(resource).body()
    }

    /**
     * Fetches a specific task by ID.
     *
     * @param taskId The ID of the task
     * @return TaskResponse containing the task details
     */
    suspend fun getTask(taskId: String): TaskResponse {
        return client.get(Tasks.Id(taskId = taskId)).body()
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return TaskResponse containing the updated task details
     */
    suspend fun updateTaskStatus(taskId: String, status: String): TaskResponse {
        val resource = Tasks.Id.Status(parent = Tasks.Id(taskId = taskId))
        val request = TaskStatusChangeRequest(status = status)
        return client.post(resource) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    /**
     * Fetches task progress for the current user.
     *
     * @param query Optional query to filter tasks by title
     * @return TaskProgressResponse containing the task progress information
     */
    suspend fun getTaskProgress(query: String? = null): TaskProgressResponse {
        return client.get(Tasks.Stats()).body()
    }

    /**
     * Creates a new task.
     *
     * @param request The task creation request
     * @return TaskResponse containing the created task details
     */
    suspend fun createTask(request: TaskCreateRequest): TaskResponse {
        return client.post(Tasks()) {
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
        return client.put(Tasks.Id(taskId = taskId)) {
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
        val response = client.delete(Tasks.Id(taskId = taskId))
        return response.status == HttpStatusCode.NoContent
    }

    /**
     * Fetches paginated tasks for a specific project.
     *
     * @param projectId The ID of the project
     * @param page The page number (0-based)
     * @param size The page size
     * @return PaginatedResponse containing the tasks for the project
     */
    suspend fun getTasksByProjectId(projectId: String, page: Int, size: Int): PaginatedResponse<TaskResponse> {
        return client.get("api/tasks/projects/$projectId") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    /**
     * Fetches tasks owned by the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @return PaginatedResponse containing the owned tasks
     */
    suspend fun getOwnedTasks(page: Int, size: Int): PaginatedResponse<TaskResponse> {
        return client.get(Tasks.Owned(page = page, size = size)).body()
    }

    /**
     * Fetches tasks assigned to the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @param query Optional query to filter tasks
     * @return PaginatedResponse containing the assigned tasks
     */
    suspend fun getAssignedTasks(page: Int, size: Int, query: String? = null): PaginatedResponse<TaskResponse> {
        return client.get(Tasks.Assigned(page = page, size = size, query = query)).body()
    }

    /**
     * Assigns a task to a user.
     *
     * @param taskId The ID of the task
     * @param assigneeId The ID of the user to assign the task to
     * @return TaskResponse containing the updated task details
     */
    suspend fun assignTask(taskId: String, assigneeId: String): TaskResponse {
        val resource = Tasks.Id.Assign(parent = Tasks.Id(taskId = taskId))
        val request = TaskAssignRequest(assigneeId = assigneeId)
        return client.post(resource) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
