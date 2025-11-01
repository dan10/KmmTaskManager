package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.domain.ProjectAssignment
import com.danioliveira.taskmanager.domain.exceptions.AlreadyAssignedException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.utils.randomV7
import com.danioliveira.taskmanager.utils.toUuid
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
class ProjectAssignmentRepositoryImpl : ProjectAssignmentRepository {

    @OptIn(ExperimentalTime::class)
    context(transaction: R2dbcTransaction)
    override suspend fun assignUserToProject(projectId: Uuid, userId: Uuid): ProjectAssignment = with(transaction) {
        val assignmentId = Uuid.randomV7().toJavaUuid()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        return try {
            ProjectAssignmentsTable.insertReturning {
                it[id] = assignmentId
                it[ProjectAssignmentsTable.projectId] = projectId.toJavaUuid()
                it[ProjectAssignmentsTable.userId] = userId.toJavaUuid()
                it[assignedAt] = now
                it[assignedBy] = userId.toJavaUuid() // For now, user assigns themselves
            }
                .map { it.toResponse() }
                .single()
        } catch (_: Exception) {
            throw AlreadyAssignedException("User is already assigned to this project")
        }
    }

    context(transaction: R2dbcTransaction)
    override suspend fun removeUserFromProject(projectId: Uuid, userId: Uuid): Boolean = with(transaction) {
        return ProjectAssignmentsTable.deleteWhere {
            ProjectAssignmentsTable.projectId eq projectId.toJavaUuid() and
                    (ProjectAssignmentsTable.userId eq userId.toJavaUuid())
        } > 0
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findUsersByProject(projectId: Uuid): List<Uuid> = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.userId)
            .where { ProjectAssignmentsTable.projectId eq projectId.toJavaUuid() }
            .map { it[ProjectAssignmentsTable.userId].value.toKotlinUuid() }
            .toList()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun findProjectsByUser(userId: Uuid): List<Uuid> = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.projectId)
            .where { ProjectAssignmentsTable.userId eq userId.toJavaUuid() }
            .map { it[ProjectAssignmentsTable.projectId].value.toKotlinUuid() }
            .toList()
    }

    context(transaction: R2dbcTransaction)
    override suspend fun isUserAssignedToProject(projectId: Uuid, userId: Uuid): Boolean = with(transaction) {
        return ProjectAssignmentsTable
            .select(ProjectAssignmentsTable.id)
            .where {
                (ProjectAssignmentsTable.projectId eq projectId.toJavaUuid()) and
                        (ProjectAssignmentsTable.userId eq userId.toJavaUuid())
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
