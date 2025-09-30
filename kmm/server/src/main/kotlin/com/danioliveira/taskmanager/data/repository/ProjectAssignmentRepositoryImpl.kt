package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.domain.ProjectAssignment
import com.danioliveira.taskmanager.domain.exceptions.AlreadyAssignedException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ProjectAssignmentRepositoryImpl : ProjectAssignmentRepository {

    @OptIn(ExperimentalTime::class)
    context(transaction: R2dbcTransaction)
    override suspend fun assignUserToProject(projectId: UUID, userId: UUID): ProjectAssignment = with(transaction) {
        val assignmentId = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        return try {
            ProjectAssignmentsTable.insertReturning {
                it[id] = assignmentId
                it[ProjectAssignmentsTable.projectId] = projectId
                it[ProjectAssignmentsTable.userId] = userId
                it[assignedAt] = now
                it[assignedBy] = userId // For now, user assigns themselves
            }
                .map { it.toResponse() }
                .single()
        } catch (_: Exception) {
            throw AlreadyAssignedException("User is already assigned to this project")
        }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun removeUserFromProject(projectId: UUID, userId: UUID): Boolean = with(transaction) {
        return ProjectAssignmentsTable.deleteWhere {
            ProjectAssignmentsTable.projectId eq projectId and
                    (ProjectAssignmentsTable.userId eq userId)
        } > 0
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findUsersByProject(projectId: UUID): List<UUID> = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.userId)
            .where { ProjectAssignmentsTable.projectId eq projectId }
            .map { it[ProjectAssignmentsTable.userId].value }
            .toList()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findProjectsByUser(userId: UUID): List<UUID> = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.projectId)
            .where { ProjectAssignmentsTable.userId eq userId }
            .map { it[ProjectAssignmentsTable.projectId].value }
            .toList()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun isUserAssignedToProject(projectId: UUID, userId: UUID): Boolean = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.id)
            .where {
                (ProjectAssignmentsTable.projectId eq projectId) and
                        (ProjectAssignmentsTable.userId eq userId)
            }
            .toList()
            .isNotEmpty()
    }

    private fun ResultRow.toResponse() = ProjectAssignment(
        id = this[ProjectAssignmentsTable.id].value.toString(),
        projectId = this[ProjectAssignmentsTable.projectId].value.toString(),
        userId = this[ProjectAssignmentsTable.userId].value.toString(),
        assignedAt = this[ProjectAssignmentsTable.assignedAt].toString()
    )
}
