package com.danioliveira.taskmanager.domain.repository

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
        dueDate: String?,
        assigneeId: String?
    ): TaskResponse?

    suspend fun Transaction.delete(id: String): Boolean

    suspend fun Transaction.findAllTasksForUser(userId: String): PaginatedResponse<TaskResponse>

    /**
     * Get the task progress for a user.
     * @param userId The ID of the user.
     * @param query Optional query to filter tasks by title.
     * @return The task progress for the user.
     */
    suspend fun Transaction.getUserTaskProgress(userId: String): TaskProgressResponse
}
