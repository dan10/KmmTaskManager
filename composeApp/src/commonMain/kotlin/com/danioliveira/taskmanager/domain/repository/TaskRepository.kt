package com.danioliveira.taskmanager.domain.repository

import androidx.paging.PagingData
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskProgressResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.domain.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task operations.
 */
interface TaskRepository {
    /**
     * Gets paginated tasks for the current user using Paging3.
     *
     * @param pageSize The page size
     * @param query Optional query to filter tasks by title
     * @return Flow of PagingData containing tasks
     */
    fun getTasksStream(pageSize: Int, query: String?): Flow<PagingData<Task>>

    /**
     * Gets paginated tasks for a specific project using Paging3.
     *
     * @param projectId The ID of the project
     * @param pageSize The page size
     * @return Flow of PagingData containing tasks for the project
     */
    fun getProjectTasksStream(projectId: String, pageSize: Int): Flow<PagingData<Task>>

    /**
     * Gets paginated tasks for the current user.
     *
     * @param page The page number (0-based)
     * @param size The page size
     * @param query Optional query to filter tasks by title
     * @return Result containing paginated tasks with task progress information
     */
    suspend fun getTasks(page: Int, size: Int, query: String? = null): Result<PaginatedResponse<TaskResponse>>

    /**
     * Gets paginated tasks for a specific project.
     *
     * @param projectId The ID of the project
     * @param page The page number (0-based)
     * @param size The page size
     * @return Result containing paginated tasks for the project
     */
    suspend fun getTasksByProjectId(projectId: String, page: Int, size: Int): Result<PaginatedResponse<TaskResponse>>

    /**
     * Gets a specific task by ID.
     *
     * @param taskId The ID of the task
     * @return Result containing the task details
     */
    suspend fun getTask(taskId: String): Result<TaskResponse>

    /**
     * Updates the status of a task.
     *
     * @param taskId The ID of the task
     * @param status The new status of the task
     * @return Result containing the updated task details
     */
    suspend fun updateTaskStatus(taskId: String, status: String): Result<TaskResponse>

    /**
     * Gets task progress for the current user.
     *
     * @param query Optional query to filter tasks by title
     * @return Result containing the task progress information
     */
    suspend fun getTaskProgress(query: String? = null): Result<TaskProgressResponse>

    /**
     * Creates a new task.
     *
     * @param request The task creation request
     * @return Result containing the created task details
     */
    suspend fun createTask(request: TaskCreateRequest): Result<TaskResponse>

    /**
     * Updates an existing task.
     *
     * @param taskId The ID of the task to update
     * @param request The task update request
     * @return Result containing the updated task details
     */
    suspend fun updateTask(taskId: String, request: TaskUpdateRequest): Result<TaskResponse>

    /**
     * Deletes a task.
     *
     * @param taskId The ID of the task to delete
     * @return Result containing true if the task was deleted successfully, false otherwise
     */
    suspend fun deleteTask(taskId: String): Result<Boolean>

    /**
     * Gets files associated with a task.
     *
     * @param taskId The ID of the task
     * @return Result containing a list of file details
     */
    suspend fun getTaskFiles(taskId: String): Result<List<FileResponse>>
}
