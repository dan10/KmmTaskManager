package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.TaskStatus
import org.jetbrains.exposed.sql.Transaction

internal interface TaskRepository {
    suspend fun Transaction.findAllByProjectId(
        projectId: String?,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse>

    suspend fun Transaction.findAllByOwnerId(
        ownerId: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse>

    suspend fun Transaction.findAllByAssigneeId(
        assigneeId: String,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<TaskResponse>

    suspend fun Transaction.findById(id: String): TaskResponse?

    /**
     * Expects all mapping from DTO/String to domain types to be handled in the service layer.
     */
    suspend fun Transaction.create(
        title: String,
        description: String?,
        projectId: java.util.UUID?,
        assigneeId: java.util.UUID?,
        creatorId: java.util.UUID,
        status: TaskStatus,
        dueDate: java.time.LocalDateTime?
    ): TaskResponse

    suspend fun Transaction.update(
        id: String,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: com.danioliveira.taskmanager.domain.Priority,
        dueDate: String?,
        assigneeId: String?
    ): TaskResponse?

    suspend fun Transaction.delete(id: String): Boolean

    suspend fun Transaction.findAllTasksForUser(userId: String): PaginatedResponse<TaskResponse>

    /**
     * Get the task progress for a user.
     * @param userId The ID of the user.
     * @return The task progress for the user.
     */
    suspend fun Transaction.getUserTaskProgress(userId: String): TaskProgressResponse

    /**
     * Get files associated with a task.
     * @param taskId The ID of the task.
     * @return List of files associated with the task.
     */
    suspend fun Transaction.getTaskFiles(taskId: String): List<FileResponse>

    /**
     * Upload a file for a task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file.
     * @param fileContent The content of the file.
     * @param contentType The MIME type of the file.
     * @param uploaderId The ID of the user uploading the file.
     * @param s3Url The URL of the file in S3.
     * @return The uploaded file.
     */
    suspend fun Transaction.uploadTaskFile(
        taskId: String,
        fileName: String,
        contentType: String,
        uploaderId: String,
        s3Url: String
    ): FileResponse
}
