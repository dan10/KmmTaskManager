package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.repository.TaskRepository

class TaskService(private val repository: TaskRepository) {
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
        with(repository) {
            create(
                title = request.title,
                description = request.description,
                projectId = request.projectId?.let { java.util.UUID.fromString(it) },
                assigneeId = request.assigneeId.let { java.util.UUID.fromString(it) },
                creatorId = java.util.UUID.fromString(creatorId),
                status = com.danioliveira.taskmanager.domain.TaskStatus.valueOf(request.status),
                dueDate = request.dueDate?.let { java.time.LocalDateTime.parse(it) }
            )
        }
    }

    suspend fun update(id: String, request: TaskUpdateRequest): TaskResponse = dbQuery {
        with(repository) {
            val current = findById(id) ?: throw NotFoundException("Task", id)
            val updated = update(
                id = id,
                title = request.title ?: current.title,
                description = request.description ?: current.description,
                status = request.status ?: current.status,
                dueDate = request.dueDate ?: current.dueDate,
                assigneeId = request.assigneeId ?: current.assigneeId
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        with(repository) { delete(id) }
    }

    suspend fun assign(id: String, assigneeId: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)
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
