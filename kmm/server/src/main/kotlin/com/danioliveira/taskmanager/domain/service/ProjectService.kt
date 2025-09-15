package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.data.dbQuery2
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.UnauthorizedException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import java.util.UUID

class ProjectService(
    private val repository: ProjectRepository,
    private val userRepository: UserRepository,
    private val assignmentRepository: ProjectAssignmentRepository
) {
    suspend fun getProjectsByOwner(
        ownerId: UUID,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<ProjectResponse> =
        dbQuery2 {
            repository.findAllByOwner(ownerId, page, size, query)
        }

    suspend fun createProject(ownerId: UUID, request: ProjectCreateRequest): ProjectResponse =
        dbQuery2 {
            repository.create(
                name = request.name,
                description = request.description,
                ownerId = ownerId
            )
        }

    suspend fun getProjectById(id: UUID, userId: UUID): ProjectResponse = dbQuery2 {
        repository.findById(
            id = id,
        ) ?: throw NotFoundException("Project", id.toString())
    }

    suspend fun updateProject(id: String, request: ProjectUpdateRequest): Boolean = dbQuery2 {
        repository.update(
            id = UUID.fromString(id),
            name = request.name,
            description = request.description
        )
    }

    suspend fun updateProjectWithPermission(projectId: String, userId: UUID, request: ProjectUpdateRequest): Boolean = dbQuery2 {
        val project = repository.findById(UUID.fromString(projectId))
            ?: throw NotFoundException("Project", projectId)
        
        val isOwner = project.ownerId == userId.toString()
        val isAssignedMember = assignmentRepository.isUserAssignedToProject(UUID.fromString(projectId), userId)
        
        if (!isOwner && !isAssignedMember) {
            throw UnauthorizedException("You don't have permission to edit this project")
        }
        
        repository.update(
            id = UUID.fromString(projectId),
            name = request.name,
            description = request.description
        )
    }

    suspend fun deleteProject(id: UUID): Boolean = dbQuery2 {
        repository.delete(id)
    }

    suspend fun deleteProjectWithPermission(projectId: UUID, userId: UUID): Boolean = dbQuery2 {
        val project = repository.findById(projectId)
            ?: throw NotFoundException("Project", projectId.toString())
        
        val isOwner = project.ownerId == userId.toString()
        
        if (!isOwner) {
            throw UnauthorizedException("Only project owners can delete projects")
        }
        
        repository.delete(projectId)
    }

    suspend fun assignUserToProject(projectId: UUID, userId: UUID) = dbQuery2 {
        val projectExists = repository.existsById(id = projectId)
        if (!projectExists) {
            throw NotFoundException("Project", projectId.toString())
        }

        val userExists = userRepository.existsById(id = userId)
        if (!userExists) {
            throw NotFoundException("User", userId.toString())
        }

        assignmentRepository.assignUserToProject(projectId, userId)
    }

    suspend fun removeUserFromProject(projectId: UUID, userId: UUID): Boolean = dbQuery2 {
        assignmentRepository.removeUserFromProject(projectId, userId)
    }

    suspend fun getUsersByProject(projectId: String): List<String> = dbQuery {
        with(assignmentRepository) { findUsersByProject(UUID.fromString(projectId)).map { it.toString() } }
    }

    suspend fun getProjectsByUser(userId: String): List<String> = dbQuery {
        assignmentRepository.findProjectsByUser(UUID.fromString(userId)).map { it.toString() }
    }

    suspend fun isUserAssignedToProject(projectId: String, userId: String): Boolean = dbQuery {
        with(assignmentRepository) {
            isUserAssignedToProject(
                UUID.fromString(projectId),
                UUID.fromString(userId)
            )
        }
    }
}
