package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class TaskRepositoryImpl : TaskRepository {

    context(transaction: R2dbcTransaction)
    override suspend fun update(
        id: String,
        title: String,
        description: String?,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?,
        assigneeId: UUID?
    ): TaskResponse? = with(transaction) {
        val taskId = UUID.fromString(id)
        // Perform update using Exposed DSL
        TasksTable.update(where = { TasksTable.id eq taskId }) {
            it[TasksTable.title] = title
            it[TasksTable.description] = description
            it[TasksTable.status] = status
            it[TasksTable.priority] = priority
            it[TasksTable.dueDate] = dueDate
            it[TasksTable.assigneeId] = assigneeId
            it[TasksTable.updatedAt] = Clock.System.now()
        }
        // Return the updated task by querying it again
        return findById(id)
    }

    context(transaction: R2dbcTransaction)
    override suspend fun delete(id: UUID): Boolean = with(transaction) {
        return TasksTable.deleteWhere { TasksTable.id eq id } > 0
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findById(id: String): TaskResponse? = with(transaction) {
        val uuid = UUID.fromString(id)
        return TasksTable
            .leftJoin(ProjectsTable,
                onColumn = { TasksTable.projectId },
                otherColumn = { ProjectsTable.id }
            )
            .select(TasksTable.fields + ProjectsTable.name + ProjectsTable.id)
            .where { TasksTable.id eq uuid }
            .singleOrNull()
            ?.toResponse()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findAllByProjectId(
        projectId: UUID,
        page: Int,
        size: Int
    ): PaginatedResponse<TaskResponse> = with(transaction) {
        return queryWithPagination(
            limit = size,
            offset = page * size
        ) { TasksTable.projectId eq projectId }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findAllByOwnerId(
        ownerId: UUID,
        page: Int,
        size: Int
    ): PaginatedResponse<TaskResponse> = with(transaction) {
        return queryWithPagination(
            limit = size,
            offset = page * size
        ) { TasksTable.creatorId eq ownerId }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findAllByAssigneeId(
        assigneeId: UUID,
        page: Int,
        size: Int,
        query: String?
    ): PaginatedResponse<TaskResponse> = with(transaction) {
        var condition: Op<Boolean> = TasksTable.assigneeId eq assigneeId
        if (!query.isNullOrBlank()) {
            val searchQuery = "%${query.lowercase()}%"
            condition = condition and (
                (TasksTable.title.lowerCase() like searchQuery) or 
                (TasksTable.description.lowerCase() like searchQuery)
            )
        }

        return queryWithPagination(
            limit = size,
            offset = page * size
        ) {
            condition
        }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findAllTasksForUser(userId: UUID, page: Int, size: Int): PaginatedResponse<TaskResponse> = with(transaction) {
        return queryWithPagination(
            limit = size,
            offset = page * size
        ) { (TasksTable.creatorId eq userId) or (TasksTable.assigneeId eq userId) }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun getUserTaskProgress(userId: UUID): TaskProgressResponse = with(transaction) {
        val totalTasks = TasksTable.id.count().alias("total_tasks")
        val completedTasks = Case()
            .When(TasksTable.status eq TaskStatus.DONE, intLiteral(1))
            .Else(intLiteral(0))
            .sum()
            .alias("completed_tasks")
        
        val result = TasksTable
            .select(totalTasks, completedTasks)
            .where { (TasksTable.creatorId eq userId) or (TasksTable.assigneeId eq userId) }
            .singleOrNull()


        return TaskProgressResponse(
            totalTasks = result?.get(totalTasks)?.toInt() ?: 0 ,
            completedTasks = result?.get(completedTasks) ?: 0,
        )
    }

    @OptIn(ExperimentalTime::class)
    context(transaction: R2dbcTransaction)
    override suspend fun create(
        title: String,
        description: String?,
        projectId: UUID?,
        assigneeId: UUID?,
        creatorId: UUID,
        status: TaskStatus,
        priority: Priority,
        dueDate: LocalDateTime?
    ): TaskResponse = with(transaction) {
        val id = UUID.randomUUID()

        TasksTable.insert {
            it[TasksTable.id] = id
            it[TasksTable.title] = title
            it[TasksTable.description] = description
            it[TasksTable.projectId] = projectId
            it[TasksTable.assigneeId] = assigneeId
            it[TasksTable.creatorId] = creatorId
            it[TasksTable.status] = status
            it[TasksTable.priority] = priority
            it[TasksTable.dueDate] = dueDate
        }

        return TaskResponse(
            id = id.toString(),
            title = title,
            description = description.orEmpty(),
            status = status,
            priority = priority,
            dueDate = dueDate,
            projectId = projectId?.toString(),
            projectName = null,
            assigneeId = assigneeId.toString(),
            creatorId = creatorId.toString()
        )
    }

    private fun ResultRow.toResponse(): TaskResponse {
        return TaskResponse(
            id = this[TasksTable.id].value.toString(),
            title = this[TasksTable.title],
            description = this[TasksTable.description].orEmpty(),
            status = this[TasksTable.status],
            priority = this[TasksTable.priority],
            dueDate = this[TasksTable.dueDate],
            projectId = this[TasksTable.projectId]?.value?.toString(),
            projectName = this[ProjectsTable.name],
            assigneeId = this[TasksTable.assigneeId]?.value?.toString(),
            creatorId = this[TasksTable.creatorId].value.toString()
        )
    }

    private suspend fun queryWithPagination(
        limit: Int? = null,
        offset: Int? = null,
        predicate: () -> Op<Boolean>,
    ): PaginatedResponse<TaskResponse> {
        // Collect total count first
        val totalCount = TasksTable
            .select(TasksTable.id.count())
            .where(predicate)
            .map { it[TasksTable.id.count()] }
            .toList()
            .firstOrNull()?.toInt() ?: 0

        // Get paginated tasks with project info
        val items =  TasksTable
            .leftJoin(ProjectsTable,
                onColumn = { TasksTable.projectId },
                otherColumn = { ProjectsTable.id }
            )
            .select(TasksTable.fields + ProjectsTable.name + ProjectsTable.id)
            .where(predicate)
            .orderBy(TasksTable.dueDate, SortOrder.DESC)
            .apply { if (limit != null) limit(limit) }
            .apply { if (offset != null) offset(offset.toLong()) }
            .map { row -> row.toResponse() }
            .toList()

        return PaginatedResponse(
            items = items,
            total = totalCount,
            currentPage = if (limit != null && limit > 0) (offset ?: 0) / limit else 0,
            pageSize = items.size,
            totalPages = if (limit != null && limit > 0) ceil(totalCount.toDouble() / limit).toInt() else 1
        )
    }

}
