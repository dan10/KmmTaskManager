package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.data.dbQuery2
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.routes.toUUID
import java.util.UUID

internal class TaskService(
    private val repository: TaskRepository,
    private val projectAssignmentRepository: ProjectAssignmentRepository,
    private val projectRepository: ProjectRepository
) {
    suspend fun findAllByProjectId(projectId: String, page: Int = 0, size: Int = 10): PaginatedResponse<TaskResponse> = dbQuery {
        val uuid = UUID.fromString(projectId)
        repository.findAllByProjectId(uuid, page, size)
    }

    suspend fun findAllByOwnerId(ownerId: String, page: Int = 0, size: Int = 10): PaginatedResponse<TaskResponse> =
        dbQuery {
            with(repository) { findAllByOwnerId(ownerId, page, size) }
        }

    suspend fun findAllByAssigneeId(
        assigneeId: String,
        page: Int = 0,
        size: Int = 10,
        query: String? = null
    ): PaginatedResponse<TaskResponse> = dbQuery {
        // The repository now handles filtering by query and including progress information
        with(repository) { findAllByAssigneeId(assigneeId, page, size, query) }
    }

    suspend fun findById(id: String): TaskResponse = dbQuery {
        with(repository) {
            findById(id) ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun create(request: TaskCreateRequest, creatorId: String): TaskResponse = dbQuery {
        val creatorUUID = UUID.fromString(creatorId)
        val assigneeUUID = request.assigneeId?.let { UUID.fromString(it) } ?: creatorUUID
        val projectUUID = request.projectId?.let { UUID.fromString(it) }

        // Validate project access if project is specified
        projectUUID?.let { projectId ->
            val project = projectRepository.findById(projectId) 
                ?: throw NotFoundException("Project", projectId.toString())
            
            // Check if assignee has access (is owner or assigned member)
            val hasAccess = project.ownerId == assigneeUUID.toString() || 
                projectAssignmentRepository.isUserAssignedToProject(projectId, assigneeUUID)
            
            if (!hasAccess) {
                throw ValidationException("Assignee must be a member of the project or the project owner")
            }
        }

        repository.create(
            title = request.title,
            description = request.description,
            projectId = projectUUID,
            assigneeId = assigneeUUID,
            creatorId = creatorUUID,
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
                val assigneeUUID = UUID.fromString(newAssigneeId)
                val projectUUID = UUID.fromString(current.projectId)
                
                // Check if user is the project owner
                val project = with(projectRepository) { findById(projectUUID,) }
                val isProjectOwner = project != null && project.ownerId == assigneeUUID.toString()
                
                // Check if user is assigned to the project
                val isAssigneeInProject = with(projectAssignmentRepository) {
                    isUserAssignedToProject(projectUUID, assigneeUUID)
                }
                
                // User must be either the project owner OR a project member
                if (!isProjectOwner && !isAssigneeInProject) {
                    throw ValidationException("Assignee must be a member of the project or the project owner")
                }
            }

            val updated = update(
                id = id,
                title = request.title ?: current.title,
                description = request.description ?: current.description,
                status = request.status ?: current.status,
                priority = request.priority ?: current.priority,
                dueDate = request.dueDate ?: current.dueDate,
                assigneeId = newAssigneeId?.toUUID() ?: current.assigneeId?.toUUID()
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    suspend fun delete(id: String): Boolean = dbQuery2 {
        repository.delete(id.toUUID())
    }

    suspend fun assign(id: String, assigneeId: String): TaskResponse = dbQuery {
        val current = with(repository) { findById(id) } ?: throw NotFoundException("Task", id)

        // Validate that assignee is part of the project or is the project owner if the task has a project
        val assigneeUUID = UUID.fromString(assigneeId)
        if (current.projectId != null) {
            val projectUUID = UUID.fromString(current.projectId)
            
            // Check if user is the project owner
            val project = with(projectRepository) { findById(projectUUID,) }
            val isProjectOwner = project != null && project.ownerId == assigneeUUID.toString()
            
            // Check if user is assigned to the project
            val isAssigneeInProject = with(projectAssignmentRepository) {
                isUserAssignedToProject(projectUUID, assigneeUUID)
            }
            
            // User must be either the project owner OR a project member
            if (!isProjectOwner && !isAssigneeInProject) {
                throw ValidationException("Assignee must be a member of the project or the project owner")
            }
        }

        with(repository) {
            val updated = update(
                id = id,
                title = current.title,
                description = current.description,
                status = current.status,
                priority = current.priority,
                dueDate = current.dueDate,
                assigneeId = assigneeId.toUUID()
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
                status = TaskStatus.valueOf(status),
                priority = current.priority,
                dueDate = current.dueDate,
                assigneeId = current.assigneeId?.toUUID()
            )
            updated ?: throw NotFoundException("Task", id)
        }
    }

    /**
     * Get the task progress for a user.
     * @param userId The ID of the user.
     * @return The task progress for the user.
     */
    suspend fun getUserTaskProgress(userId: String): TaskProgressResponse = dbQuery {
        with(repository) { getUserTaskProgress(userId) }
    }
}
