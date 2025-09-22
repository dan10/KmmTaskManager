package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.ForbiddenException
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import org.jetbrains.exposed.v1.core.Transaction
import java.util.UUID

class ProjectService(
    private val repository: ProjectRepository,
    private val userRepository: UserRepository,
    private val assignmentRepository: ProjectAssignmentRepository,
) {
    suspend fun getProjectsByOwner(
        ownerId: UUID,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<ProjectResponse> =
        dbQuery {
            repository.findAllByOwner(ownerId, page, size, query)
        }

    suspend fun createProject(ownerId: UUID, request: ProjectCreateRequest): ProjectResponse =
        dbQuery {
            repository.create(
                name = request.name,
                description = request.description,
                ownerId = ownerId
            )
        }

    context(_: Transaction)
    private suspend fun validateProjectAccess(projectId: UUID, userId: UUID) {
        val project = repository.findById(projectId)

        val isOwner = project.ownerId == userId.toString()
        val isAssignedMember = assignmentRepository.isUserAssignedToProject(projectId, userId)

        if (!isOwner && !isAssignedMember) {
            throw ForbiddenException(resourceType = "Project", resourceId = projectId.toString())
        }
    }

    suspend fun getProjectById(id: UUID, userId: UUID): ProjectResponse = dbQuery {
        validateProjectAccess(id, userId)
        repository.findById(id = id)
    }

    suspend fun updateProject(
        projectId: UUID,
        userId: UUID,
        request: ProjectUpdateRequest
    ): Boolean = dbQuery {
        validateProjectAccess(projectId, userId)
        repository.update(
            id = projectId,
            name = request.name,
            description = request.description
        )
    }

    suspend fun deleteProject(projectId: UUID, userId: UUID): Boolean = dbQuery {
        val project = repository.findById(projectId)

        if (project.ownerId != userId.toString()) {
            throw ForbiddenException(resourceType = "Project", resourceId = projectId.toString())
        }

        repository.delete(projectId)
    }

    suspend fun assignUserToProject(projectId: UUID, userId: UUID, creatorId: UUID) = dbQuery {
        val projectExists = repository.existsById(id = projectId)
        if (!projectExists) {
            throw NotFoundException("Project", projectId.toString())
        }

        val userExists = userRepository.existsById(id = userId)
        if (!userExists) {
            throw NotFoundException("User", userId.toString())
        }

        validateProjectAccess(projectId, creatorId)
        assignmentRepository.assignUserToProject(projectId, userId)
    }

    suspend fun removeUserFromProject(projectId: UUID, userId: UUID, creatorId: UUID): Boolean =
        dbQuery {
            validateProjectAccess(projectId, creatorId)
            assignmentRepository.removeUserFromProject(projectId, userId)
        }

    suspend fun getUsersByProject(projectId: UUID): List<String> = dbQuery {
        assignmentRepository.findUsersByProject(projectId).map { it.toString() }
    }

    suspend fun getProjectsByUser(userId: UUID): List<String> = dbQuery {
        assignmentRepository.findProjectsByUser(userId).map { it.toString() }
    }

    suspend fun isUserAssignedToProject(projectId: String, userId: String): Boolean = dbQuery {
        assignmentRepository.isUserAssignedToProject(
            UUID.fromString(projectId),
            UUID.fromString(userId)
        )
    }
}
