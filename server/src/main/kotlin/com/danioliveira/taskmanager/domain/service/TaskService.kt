package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.utils.FileValidator
import com.danioliveira.taskmanager.utils.S3ClientFactory
import java.util.UUID

internal class TaskService(
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
                status = com.danioliveira.taskmanager.domain.TaskStatus.TODO, // Default status for new tasks
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
                priority = request.priority ?: current.priority,
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
                priority = current.priority,
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
                priority = current.priority,
                dueDate = current.dueDate,
                assigneeId = current.assigneeId
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

    /**
     * Get files associated with a task.
     * @param taskId The ID of the task.
     * @return List of files associated with the task.
     */
    suspend fun getTaskFiles(taskId: String): List<FileResponse> = dbQuery {
        with(repository) { getTaskFiles(taskId) }
    }

    /**
     * Upload a file for a task.
     * @param taskId The ID of the task.
     * @param fileName The name of the file.
     * @param contentType The MIME type of the file.
     * @param fileBytes The content of the file.
     * @param uploaderId The ID of the user uploading the file.
     * @return The uploaded file.
     * @throws ValidationException If the file type is not allowed.
     */
    suspend fun uploadTaskFile(
        taskId: String,
        fileName: String,
        contentType: String,
        fileBytes: ByteArray,
        uploaderId: String
    ): FileResponse = dbQuery {
        // Validate file type
        if (!FileValidator.isValidMimeType(contentType)) {
            throw ValidationException("File type not allowed. Allowed types: ${FileValidator.getAllowedMimeTypesAsString()}")
        }

        // Check if task exists
        findById(taskId) // This will throw NotFoundException if task doesn't exist

        // Upload file to MinIO
        val s3Client = S3ClientFactory.createFromEnv()
        val s3Url = s3Client.uploadFile(fileName, contentType, fileBytes)

        // Save file metadata to database
        with(repository) {
            uploadTaskFile(
                taskId = taskId,
                fileName = fileName,
                contentType = contentType,
                uploaderId = uploaderId,
                s3Url = s3Url
            )
        }
    }
}
