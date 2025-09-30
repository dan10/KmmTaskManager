package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import java.util.UUID

internal interface TaskRepository {
    context(transaction: R2dbcTransaction)
    suspend fun findAllByProjectId(
        projectId: UUID,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse>

    context(transaction: R2dbcTransaction)
    suspend fun findAllByOwnerId(
        ownerId:  UUID,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse>

    context(transaction: R2dbcTransaction)
    suspend fun findAllByAssigneeId(
        assigneeId: UUID,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<TaskResponse>

    context(transaction: R2dbcTransaction)
    suspend fun findById(id: String): TaskResponse?

    /**
     * Expects all mapping from DTO/String to domain types to be handled in the service layer.
     */
    context(transaction: R2dbcTransaction)
    suspend fun create(
        title: String,
        description: String?,
        projectId: UUID?,
        assigneeId: UUID?,
        creatorId: UUID,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?
    ): TaskResponse

    context(transaction: R2dbcTransaction)
    suspend fun update(
        id: String,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?,
        assigneeId: UUID?
    ): TaskResponse?

    context(transaction: R2dbcTransaction)
    suspend fun delete(id: UUID): Boolean

    context(transaction: R2dbcTransaction)
    suspend fun findAllTasksForUser(userId: UUID, page: Int, size: Int): PaginatedResponse<TaskResponse>

    /**
     * Get the task progress for a user.
     * @param userId The ID of the user.
     * @return The task progress for the user.
     */
    context(transaction: R2dbcTransaction)
    suspend fun getUserTaskProgress(userId: UUID): TaskProgressResponse
}
