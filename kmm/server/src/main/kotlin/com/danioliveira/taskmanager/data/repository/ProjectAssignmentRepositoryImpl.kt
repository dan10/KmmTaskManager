package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.data.entity.ProjectAssignmentDAOEntity
import com.danioliveira.taskmanager.data.entity.ProjectDAOEntity
import com.danioliveira.taskmanager.data.entity.UserDAOEntity
import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.domain.ProjectAssignment
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import java.util.UUID

class ProjectAssignmentRepositoryImpl : ProjectAssignmentRepository {

    override suspend fun Transaction.assignUserToProject(projectId: UUID, userId: UUID): ProjectAssignment {
        // Check if project and user exist
        val project = ProjectDAOEntity.findById(projectId) ?: throw IllegalArgumentException("Project not found")
        val user = UserDAOEntity.findById(userId) ?: throw IllegalArgumentException("User not found")

        // Check if assignment already exists
        val existingAssignment = ProjectAssignmentDAOEntity.find {
            (ProjectAssignmentsTable.project eq projectId) and (ProjectAssignmentsTable.user eq userId)
        }.firstOrNull()

        if (existingAssignment != null) {
            throw IllegalStateException("User is already assigned to this project")
        }

        // Create new assignment
        val assignmentId = UUID.randomUUID()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val assignment = ProjectAssignmentDAOEntity.new(assignmentId) {
            this.project = project
            this.user = user
            this.assignedAt = now
        }

        return assignment.toDomain()
    }

    override suspend fun Transaction.removeUserFromProject(projectId: UUID, userId: UUID): Boolean {
        val assignment = ProjectAssignmentDAOEntity.find {
            (ProjectAssignmentsTable.project eq projectId) and (ProjectAssignmentsTable.user eq userId)
        }.firstOrNull() ?: return false

        assignment.delete()
        return true
    }

    override suspend fun Transaction.findUsersByProject(projectId: UUID): List<UUID> {
        return ProjectAssignmentDAOEntity.find {
            ProjectAssignmentsTable.project eq projectId
        }.map { it.user.id.value }
    }

    override suspend fun Transaction.findProjectsByUser(userId: UUID): List<UUID> {
        return ProjectAssignmentDAOEntity.find {
            ProjectAssignmentsTable.user eq userId
        }.map { it.project.id.value }
    }

    override suspend fun Transaction.isUserAssignedToProject(projectId: UUID, userId: UUID): Boolean {
        return ProjectAssignmentDAOEntity.find {
            (ProjectAssignmentsTable.project eq projectId) and (ProjectAssignmentsTable.user eq userId)
        }.count() > 0
    }

    private fun ProjectAssignmentDAOEntity.toDomain(): ProjectAssignment {
        return ProjectAssignment(
            id = this.id.value.toString(),
            projectId = this.project.id.value.toString(),
            userId = this.user.id.value.toString(),
            assignedAt = this.assignedAt.toString()
        )
    }
}
