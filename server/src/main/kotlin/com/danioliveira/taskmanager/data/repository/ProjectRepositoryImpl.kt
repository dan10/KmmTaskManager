package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.entity.ProjectDAOEntity
import com.danioliveira.taskmanager.data.entity.UserDAOEntity
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Transaction
import java.time.LocalDateTime
import java.util.*
import kotlin.math.ceil

class ProjectRepositoryImpl : ProjectRepository {

    override suspend fun Transaction.create(name: String, description: String?, ownerId: UUID): ProjectResponse {
        val owner = UserDAOEntity.findById(ownerId) ?: throw IllegalArgumentException("Owner not found")
        val entity = ProjectDAOEntity.new {
            this.name = name
            this.description = description
            this.owner = owner.id
            this.createdAt = LocalDateTime.now()
        }
        return entity.toResponse()
    }

    override suspend fun Transaction.findById(id: UUID): ProjectResponse? =
        ProjectDAOEntity.findById(id)?.toResponse()

    override suspend fun Transaction.findByOwner(
        ownerId: UUID,
        page: Int,
        size: Int
    ): PaginatedResponse<ProjectResponse> {
        val query = ProjectDAOEntity.find { ProjectsTable.owner eq ownerId }
        return query.toPaginatedResponse(page, size)
    }

    override suspend fun Transaction.findAll(page: Int, size: Int): PaginatedResponse<ProjectResponse> {
        val query = ProjectDAOEntity.all()
        return query.toPaginatedResponse(page, size)
    }

    override suspend fun Transaction.update(id: UUID, name: String, description: String?): Boolean {
        val entity = ProjectDAOEntity.findById(id) ?: return false
        entity.name = name
        entity.description = description
        return true
    }

    override suspend fun Transaction.delete(id: UUID): Boolean =
        ProjectDAOEntity.findById(id)?.let { it.delete(); true } ?: false

    private fun SizedIterable<ProjectDAOEntity>.toPaginatedResponse(
        page: Int,
        size: Int
    ): PaginatedResponse<ProjectResponse> {
        val total = this.count()
        val totalPages = if (size > 0) ceil(total.toDouble() / size).toInt() else 0
        val items = this.orderBy(ProjectsTable.createdAt to SortOrder.DESC)
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

    private fun ProjectDAOEntity.toResponse() = ProjectResponse(
        id = this.id.value.toString(),
        name = this.name,
        description = this.description,
        ownerId = this.owner.toString(),
        createdAt = this.createdAt.toString()
    )
}
