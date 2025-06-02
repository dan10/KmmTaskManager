package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.entity.ProjectDAOEntity
import com.danioliveira.taskmanager.data.entity.TaskDAOEntity
import com.danioliveira.taskmanager.data.entity.UserDAOEntity
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import java.util.UUID
import org.jetbrains.exposed.sql.lowerCase
import kotlin.math.ceil

internal class TaskRepositoryImpl : TaskRepository {

    override suspend fun Transaction.update(
        id: String,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?,
        assigneeId: String?
    ): TaskResponse? {
        val uuid = UUID.fromString(id)
        val entity = TaskDAOEntity.findById(uuid) ?: return null
        entity.title = title
        entity.description = description
        entity.status = status
        entity.priority = priority
        entity.dueDate = dueDate
        entity.assignee = assigneeId?.let { UUID.fromString(it) }?.let { UserDAOEntity.findById(it) }
        return entity.toResponse()
    }

    override suspend fun Transaction.delete(id: String): Boolean {
        val uuid = UUID.fromString(id)
        return TaskDAOEntity.findById(uuid)?.let { it.delete(); true } ?: false
    }

    override suspend fun Transaction.findById(id: String): TaskResponse? {
        val uuid = UUID.fromString(id)
        return TaskDAOEntity.findById(uuid)?.toResponse()
    }

    override suspend fun Transaction.findAllByProjectId(
        projectId: String?,
        page: Int,
        size: Int
    ): PaginatedResponse<TaskResponse> {
        val query = if (projectId != null) {
            val uuid = UUID.fromString(projectId)
            TaskDAOEntity.find { TasksTable.project eq uuid }
        } else {
            TaskDAOEntity.all()
        }

        return query.toPaginatedResponse(page, size)
    }

    override suspend fun Transaction.findAllByOwnerId(
        ownerId: String,
        page: Int,
        size: Int
    ): PaginatedResponse<TaskResponse> {
        val uuid = UUID.fromString(ownerId)
        val query = TaskDAOEntity.find { TasksTable.creator eq uuid }

        return query.toPaginatedResponse(page, size)
    }

    override suspend fun Transaction.findAllByAssigneeId(
        assigneeId: String,
        page: Int,
        size: Int,
        query: String?
    ): PaginatedResponse<TaskResponse> {
        val assigneeUuid = UUID.fromString(assigneeId)

        var condition: Op<Boolean> = TasksTable.assignee eq assigneeUuid
        if (!query.isNullOrBlank()) {
            val searchQuery = "%${query.lowercase()}%"
            condition = condition and (
                (TasksTable.title.lowerCase() like searchQuery) or 
                (TasksTable.description.lowerCase() like searchQuery)
            )
        }

        val taskQuery = TaskDAOEntity.find { condition }
        return taskQuery.toPaginatedResponse(page, size)

    }

    override suspend fun Transaction.findAllTasksForUser(userId: String): PaginatedResponse<TaskResponse> {
        val uuid = UUID.fromString(userId)
        val query = TaskDAOEntity.find { (TasksTable.creator eq uuid) or (TasksTable.assignee eq uuid) }

        return query.toPaginatedResponse(0, 100) // Default to first page with 100 items
    }

    override suspend fun Transaction.getUserTaskProgress(userId: String): TaskProgressResponse {
        val uuid = UUID.fromString(userId)
        val userTasks = TaskDAOEntity.find { (TasksTable.creator eq uuid) or (TasksTable.assignee eq uuid) }

        val totalTasks = userTasks.count()
        val completedTasks = userTasks.count { it.status == TaskStatus.DONE }

        return TaskProgressResponse(
            totalTasks = totalTasks.toInt(),
            completedTasks = completedTasks,
        )
    }

    private fun SizedIterable<TaskDAOEntity>.toPaginatedResponse(
        page: Int,
        size: Int
    ): PaginatedResponse<TaskResponse> {
        val total = this.count()
        val totalPages = if (size > 0) ceil(total.toDouble() / size).toInt() else 0
        val items = this.orderBy(TasksTable.createdAt to SortOrder.DESC)
            .limit(size)
            .offset((page * size).toLong())
            .map { it.toResponse() }

        return PaginatedResponse(
            items = items,
            total = total,
            page = page,
            size = size,
            totalPages = totalPages
        )
    }

    override suspend fun Transaction.create(
        title: String,
        description: String?,
        projectId: UUID?,
        assigneeId: UUID?,
        creatorId: UUID,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?
    ): TaskResponse {
        val creator = UserDAOEntity.findById(creatorId) ?: throw IllegalArgumentException("Creator not found")
        val entity = TaskDAOEntity.new(UUID.randomUUID()) {
            this.title = title
            this.description = description
            this.project =
                projectId?.let { ProjectDAOEntity.findById(it) ?: throw IllegalArgumentException("Project not found") }
            this.assignee = assigneeId?.let { UserDAOEntity.findById(it) }
            this.creator = creator
            this.status = status
            this.priority = priority
            this.dueDate = dueDate
            this.createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }
        return entity.toResponse()
    }

    private fun TaskDAOEntity.toResponse(): TaskResponse {
        return TaskResponse(
            id = this.id.value.toString(),
            title = this.title,
            description = this.description.orEmpty(),
            status = this.status,
            priority = this.priority,
            dueDate = this.dueDate,
            projectId = this.project?.id?.value?.toString(),
            projectName = this.project?.name,
            assigneeId = this.assignee?.id?.value?.toString(),
            creatorId = this.creator.id.value.toString()
        )
    }
}
