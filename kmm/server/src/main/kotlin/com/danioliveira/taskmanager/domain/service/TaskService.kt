package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.ForbiddenException
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.utils.toUuid
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class TaskService(
    private val repository: TaskRepository,
    private val projectAssignmentRepository: ProjectAssignmentRepository,
    private val projectRepository: ProjectRepository,
) {
    suspend fun findAllByProjectId(
        projectId: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse> = dbQuery {
        val uuid = projectId.toUuid()
        repository.findAllByProjectId(uuid, page, size)
    }

    suspend fun findAllByOwnerId(
        ownerId: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse> = dbQuery {
        repository.findAllByOwnerId(ownerId.toUuid(), page, size)
    }

    suspend fun findAllByAssigneeId(
        assigneeId: Uuid,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<TaskResponse> = dbQuery {
        addLogger(Slf4jSqlDebugLogger)
        with(this) {
            repository.findAllByAssigneeId(assigneeId, page, size, query)
        }
    }

    suspend fun findAssignedDueOn(
        assigneeId: Uuid,
        date: String,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<TaskResponse> = dbQuery {
        // Parse date string (YYYY-MM-DD) to LocalDate
        val localDate = kotlinx.datetime.LocalDate.parse(date)
        // Create start of day and end of day (next day at 00:00)
        val start = LocalDateTime(localDate.year, localDate.monthNumber, localDate.dayOfMonth, 0, 0, 0)
        val nextDay = localDate.plus(DatePeriod(days = 1))
        val end = LocalDateTime(nextDay.year, nextDay.monthNumber, nextDay.dayOfMonth, 0, 0, 0)
        
        with(this) {
            repository.findAllByAssigneeAndDueDateRange(assigneeId, start, end, page, size)
        }
    }

    suspend fun findById(id: String): TaskResponse = dbQuery {
        with(repository) {
            findById(id) ?: throw NotFoundException("Task", id)
        }
    }

    /**
     * Validates that a user can be assigned to a project.
     * A user can be assigned if they are either the project owner or already assigned to the project.
     */
    context(_: R2dbcTransaction)
    private suspend fun validateUserCanBeAssignedToProject(projectId: Uuid, userId: Uuid) = with(this) {
        val project = projectRepository.findById(projectId)
        val isOwner = project.ownerId == userId.toString()
        if (isOwner) return@with

        if (!projectAssignmentRepository.isUserAssignedToProject(projectId, userId)) {
            throw ForbiddenException(resourceType = "Project", resourceId = projectId.toString())
        }
    }

    suspend fun create(request: TaskCreateRequest, creatorId: String): TaskResponse = dbQuery {
        val creatorUUID = creatorId.toUuid()
        val assigneeUUID = request.assigneeId?.toUuid() ?: creatorUUID
        val projectUUID = request.projectId?.toUuid()

        // Validate project access if project is specified
        projectUUID?.let { projectId ->
            validateUserCanBeAssignedToProject(projectId, assigneeUUID)
        }

        repository.create(
            title = request.title,
            description = request.description,
            projectId = projectUUID,
            assigneeId = assigneeUUID,
            creatorId = creatorId.toUuid(),
            status = TaskStatus.TODO,
            priority = request.priority,
            dueDate = request.dueDate
        )
    }

    suspend fun update(id: String, request: TaskUpdateRequest): TaskResponse = dbQuery {
        with(repository) {
            val current = findById(id) ?: throw NotFoundException("Task", id)

            // If assignee is being changed and task has a project, validate that new assignee is part of the project or is the project owner
            val newAssigneeId = request.assigneeId
            if (newAssigneeId != null && newAssigneeId != current.assigneeId && current.projectId != null) {
                val assigneeUUID = newAssigneeId.toUuid()
                val currentProjectId = current.projectId // Create local variable to enable smart cast
                val projectUUID = currentProjectId!!.toUuid()

                validateUserCanBeAssignedToProject(projectUUID, assigneeUUID)
            }

            val updated = update(
                id = id,
                title = request.title ?: current.title,
                description = request.description ?: current.description,
                status = request.status ?: current.status,
                priority = request.priority ?: current.priority,
                dueDate = request.dueDate ?: current.dueDate,
                assigneeId = newAssigneeId?.toUuid() ?: current.assigneeId?.toUuid()
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        // TODO: Validate that the user has permission to delete the task (e.g., is the creator or participant in the project of the task)
        repository.delete(id.toUuid())
    }

    suspend fun assign(id: String, assigneeId: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)

        // Validate that assignee is part of the project or is the project owner if the task has a project
        val assigneeUUID = assigneeId.toUuid()
        if (current.projectId != null) {
            val currentProjectId = current.projectId // Create local variable to enable smart cast
            val projectUUID = currentProjectId!!.toUuid()
            validateUserCanBeAssignedToProject(projectUUID, assigneeUUID)
        }

        with(repository) {
            val updated = update(
                id = id,
                title = current.title,
                description = current.description,
                status = current.status,
                priority = current.priority,
                dueDate = current.dueDate,
                assigneeId = assigneeId.toUuid()
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun changeStatus(id: String, status: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)
        repository.update(
            id = id,
            title = current.title,
            description = current.description,
            status = TaskStatus.valueOf(status),
            priority = current.priority,
            dueDate = current.dueDate,
            assigneeId = current.assigneeId?.toUuid()
        ) ?: throw NotFoundException("Task", id)
    }

    /**
     * Get the task progress for a user.
     * @param userId The ID of the user.
     * @return The task progress for the user.
     */
    suspend fun getUserTaskProgress(userId: Uuid): TaskProgressResponse = dbQuery {
        with(this) {
            repository.getUserTaskProgress(userId)
        }
    }
}
