package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import java.util.UUID

class ProjectService(
    private val repository: ProjectRepository,
    private val assignmentRepository: ProjectAssignmentRepository
) {
    suspend fun getProjectsByOwner(
        ownerId: String,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<ProjectResponse> =
        dbQuery {
            with(repository) { findByOwner(UUID.fromString(ownerId), page, size, query) }
        }

    suspend fun getAllProjects(
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<ProjectResponse> = dbQuery {
        with(repository) { findAll(page, size, query) }
    }

    suspend fun createProject(ownerId: String, request: ProjectCreateRequest): ProjectResponse = dbQuery {
        with(repository) { create(request.name, request.description, UUID.fromString(ownerId)) }
    }

    suspend fun getProjectById(id: String): ProjectResponse = dbQuery {
        with(repository) {
            findById(UUID.fromString(id)) ?: throw NotFoundException("Project", id)
        }
    }

    suspend fun updateProject(id: String, request: ProjectUpdateRequest): Boolean = dbQuery {
        with(repository) { update(UUID.fromString(id), request.name, request.description) }
    }

    suspend fun deleteProject(id: String): Boolean = dbQuery {
        with(repository) { delete(UUID.fromString(id)) }
    }

    suspend fun assignUserToProject(projectId: String, userId: String) = dbQuery {
        with(assignmentRepository) { assignUserToProject(UUID.fromString(projectId), UUID.fromString(userId)) }
    }

    suspend fun removeUserFromProject(projectId: String, userId: String): Boolean = dbQuery {
        with(assignmentRepository) { removeUserFromProject(UUID.fromString(projectId), UUID.fromString(userId)) }
    }

    suspend fun getUsersByProject(projectId: String): List<String> = dbQuery {
        with(assignmentRepository) { findUsersByProject(UUID.fromString(projectId)).map { it.toString() } }
    }

    suspend fun getProjectsByUser(userId: String): List<String> = dbQuery {
        with(assignmentRepository) { findProjectsByUser(UUID.fromString(userId)).map { it.toString() } }
    }

    suspend fun isUserAssignedToProject(projectId: String, userId: String): Boolean = dbQuery {
        with(assignmentRepository) { isUserAssignedToProject(UUID.fromString(projectId), UUID.fromString(userId)) }
    }
}
