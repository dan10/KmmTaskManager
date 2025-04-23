package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import java.util.*

class TaskService(
    private val repository: TaskRepository,
    private val projectAssignmentRepository: ProjectAssignmentRepository
) {
    suspend fun findAll(projectId: String?, page: Int = 0, size: Int = 10): PaginatedResponse<TaskResponse> = dbQuery {
        with(repository) { findAllByProjectId(projectId, page, size) }
    }

    suspend fun findAllByOwnerId(ownerId: String, page: Int = 0, size: Int = 10): PaginatedResponse<TaskResponse> =
        dbQuery {
            with(repository) { findAllByOwnerId(ownerId, page, size) }
        }

    suspend fun findAllByAssigneeId(
        assigneeId: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse> = dbQuery {
        with(repository) { findAllByAssigneeId(assigneeId, page, size) }
    }

    suspend fun findById(id: String): TaskResponse = dbQuery {
        with(repository) {
            findById(id) ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun create(request: TaskCreateRequest, creatorId: String): TaskResponse = dbQuery {
        val creatorUUID = UUID.fromString(creatorId)

        // Rule 1: If assigneeId is not provided, set it to the creator
        val assigneeUUID = if (request.assigneeId == null) {
            creatorUUID
        } else {
            UUID.fromString(request.assigneeId)
        }

        // Rule 2: If project is specified, validate that assignee is part of the project
        val projectUUID = request.projectId?.let { UUID.fromString(it) }
        if (projectUUID != null && assigneeUUID != null) {
            val isAssigneeInProject = with(projectAssignmentRepository) {
                isUserAssignedToProject(projectUUID, assigneeUUID)
            }
            if (!isAssigneeInProject) {
                throw ValidationException("Assignee must be a member of the project")
            }
        }

        with(repository) {
            create(
                title = request.title,
                description = request.description,
                projectId = projectUUID,
                assigneeId = assigneeUUID,
                creatorId = creatorUUID,
                status = com.danioliveira.taskmanager.domain.TaskStatus.valueOf(request.status),
                dueDate = request.dueDate?.let { java.time.LocalDateTime.parse(it) }
            )
        }
    }

    suspend fun update(id: String, request: TaskUpdateRequest): TaskResponse = dbQuery {
        with(repository) {
            val current = findById(id) ?: throw NotFoundException("Task", id)

            // If assignee is being changed and task has a project, validate that new assignee is part of the project
            val newAssigneeId = request.assigneeId
            if (newAssigneeId != null && newAssigneeId != current.assigneeId && current.projectId != null) {
                val assigneeUUID = UUID.fromString(newAssigneeId)
                val projectUUID = UUID.fromString(current.projectId)
                val isAssigneeInProject = with(projectAssignmentRepository) {
                    isUserAssignedToProject(projectUUID, assigneeUUID)
                }
                if (!isAssigneeInProject) {
                    throw ValidationException("Assignee must be a member of the project")
                }
            }

            val updated = update(
                id = id,
                title = request.title ?: current.title,
                description = request.description ?: current.description,
                status = request.status ?: current.status,
                dueDate = request.dueDate ?: current.dueDate,
                assigneeId = newAssigneeId ?: current.assigneeId
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        with(repository) { delete(id) }
    }

    suspend fun assign(id: String, assigneeId: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)

        // Validate that assignee is part of the project if the task has a project
        val assigneeUUID = UUID.fromString(assigneeId)
        if (current.projectId != null) {
            val projectUUID = UUID.fromString(current.projectId)
            val isAssigneeInProject = with(projectAssignmentRepository) {
                isUserAssignedToProject(projectUUID, assigneeUUID)
            }
            if (!isAssigneeInProject) {
                throw ValidationException("Assignee must be a member of the project")
            }
        }

        with(repository) {
            val updated = update(
                id = id,
                title = current.title,
                description = current.description,
                status = current.status,
                dueDate = current.dueDate,
                assigneeId = assigneeId
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun changeStatus(id: String, status: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)
        with(repository) {
            val updated = update(
                id = id,
                title = current.title,
                description = current.description,
                status = com.danioliveira.taskmanager.domain.TaskStatus.valueOf(status),
                dueDate = current.dueDate,
                assigneeId = current.assigneeId
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }
}
