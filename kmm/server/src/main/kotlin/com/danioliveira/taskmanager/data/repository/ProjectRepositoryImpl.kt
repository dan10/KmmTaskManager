package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Case
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.UUID
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Implementation of the ProjectRepository interface.
 *
 * This implementation is optimized for performance by:
 * 1. Batching task count calculations to reduce database queries
 * 2. Using proper transaction context for all database operations
 * 3. Implementing efficient pagination with proper sorting
 */
class ProjectRepositoryImpl : ProjectRepository {

    @OptIn(ExperimentalTime::class)
    context(transaction: Transaction)
    override suspend fun create(name: String, description: String?, ownerId: UUID): ProjectResponse {
       return ProjectsTable.insertReturning {
            it[this.name] = name
            it[this.description] = description
            it[this.ownerId] = ownerId
        }
           .map { it.toResponse() }
           .single()
    }

    context(transaction: Transaction)
    override suspend fun findById(id: UUID): ProjectResponse? {
        val searchQuery = ProjectsTable.id eq id
        val query = buildProjectQuery(ownerId = null, searchQuery, null, null)
        return query.first.firstOrNull()
    }

    override suspend fun existsById(id: UUID): Boolean {
        return ProjectsTable
            .select(ProjectsTable.id)
            .where { ProjectsTable.id eq id }
            .firstOrNull() != null
    }

    context(transaction: Transaction)
    override suspend fun findAllByOwner(
        ownerId: UUID,
        page: Int,
        size: Int,
        query: String?
    ): PaginatedResponse<ProjectResponse> {
        val searchQuery: Op<Boolean>? = if (!query.isNullOrBlank()) {
            val q = "%${query.lowercase()}%"
            (ProjectsTable.name.lowerCase() like q)
        } else null

        val query = buildProjectQuery(ownerId, searchQuery, page, size)
        
        val list = query.first.toList()
        
        return PaginatedResponse(
            items = list,
            total = list.size,
            currentPage = page,
            pageSize = size,
            totalPages = query.second
        )
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun buildProjectQuery(
        ownerId: UUID?,
        subQuery: Op<Boolean>?,
        page: Int?,
        size: Int?,
    ): Pair<Flow<ProjectResponse>, Int> {
        val total = if (page != null) ProjectsTable.id.count().over().alias("total") else null

        val tasksCount = TasksTable.id.count().alias("total_tasks")

        val completed = Case()
            .When(TasksTable.status eq TaskStatus.DONE, intLiteral(1))
            .Else(intLiteral(0))
            .count()
            .alias("completed_tasks")

        val inProgress = Case()
            .When(TasksTable.status eq TaskStatus.IN_PROGRESS, intLiteral(1))
            .Else(intLiteral(0))
            .count()
            .alias("in_progress_tasks")

        val list = listOfNotNull(total, tasksCount, completed, inProgress)

        val query = (ProjectsTable leftJoin TasksTable)
            .select(ProjectsTable.fields + list)
            .apply { if (ownerId != null) andWhere { ProjectsTable.ownerId eq ownerId } }
            .apply { if (subQuery != null) andWhere { subQuery } }
            .apply { groupBy(ProjectsTable.id) }
            .apply { if (page != null) orderBy(ProjectsTable.createdAt, SortOrder.DESC) }
            .apply { if (size != null) limit(size) }
            .apply { if (page != null && size != null) offset((page * size).toLong()) }
        
           val items =  query.map { row ->
               row.toResponse(
                   totalTasks = row[tasksCount].toInt(),
                   completedTasks = row[completed].toInt(),
                   inProgressTasks = row[inProgress].toInt()
               ) 
           }

        val totalPages = if (total != null && size != null)
            ceil((query.firstOrNull()?.get(total)?.toInt() ?: 0).toDouble() / size).toInt()
        else 0

        return items to totalPages
    }
    

    @OptIn(ExperimentalTime::class)
    context(transaction: Transaction)
    override suspend fun update(id: UUID, name: String, description: String?): Boolean {
        return ProjectsTable.update({ ProjectsTable.id eq id }) {
            it[this.name] = name
            it[this.description] = description
            it[this.updatedAt] = Clock.System.now()
        } > 0
    }

    context(transaction: Transaction)
    override suspend fun delete(id: UUID): Boolean =
        ProjectsTable.deleteWhere { ProjectsTable.id eq id } > 0

    @OptIn(ExperimentalTime::class)
    private fun ResultRow.toResponse(
        totalTasks: Int = 0,
        completedTasks: Int = 0,
        inProgressTasks: Int = 0
    ) = ProjectResponse(
        id = this[ProjectsTable.id].value.toString(),
        name = this[ProjectsTable.name],
        description = this[ProjectsTable.description],
        ownerId = this[ProjectsTable.ownerId].toString(),
        createdAt = this[ProjectsTable.createdAt].toString(),
        completed = completedTasks,
        inProgress = inProgressTasks,
        totalTasks = totalTasks
    )
}
