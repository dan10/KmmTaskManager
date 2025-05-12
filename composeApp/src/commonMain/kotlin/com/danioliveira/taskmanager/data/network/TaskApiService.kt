package com.danioliveira.taskmanager.data.network

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
        return client.get("api/tasks") {
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
        return client.get("api/tasks/$taskId").body()
    }

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return TaskResponse containing the updated task details
     */
    suspend fun updateTaskStatus(taskId: String, status: String): TaskResponse {
        return client.post("api/tasks/$taskId/status") {
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
        return client.get("api/tasks/progress") {
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
        return client.post("api/tasks") {
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
        return client.put("api/tasks/$taskId") {
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
        val response = client.delete("api/tasks/$taskId")
        return response.status == HttpStatusCode.NoContent
    }

    /**
     * Fetches files associated with a task.
     *
     * @param taskId The ID of the task
     * @return List of FileResponse containing the file details
     */
    suspend fun getTaskFiles(taskId: String): List<FileResponse> {
        return client.get("api/tasks/$taskId/files").body()
    }

    /**
     * Uploads a file for a task.
     *
     * @param taskId The ID of the task
     * @param fileName The name of the file
     * @param fileContent The content of the file as ByteArray
     * @param contentType The MIME type of the file (e.g., "image/jpeg", "application/pdf")
     * @return FileResponse containing the uploaded file details
     */
    suspend fun uploadTaskFile(
        taskId: String,
        fileName: String,
        fileContent: ByteArray,
        contentType: String
    ): FileResponse {
        return client.post("api/tasks/$taskId/files") {
            contentType(ContentType.MultiPart.FormData)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", fileContent, Headers.build {
                            append(HttpHeaders.ContentType, contentType)
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        })
                    }
                )
            )
        }.body()
    }
}
