package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.ProjectResponse
import org.jetbrains.exposed.sql.Transaction
import java.util.*

interface ProjectRepository {

    /**
     * Creates a new project
     *
     * @param name the name of the project
     * @param description the description of the project
     * @param ownerId the owner of the project
     * @return the newly created project
     */
    suspend fun Transaction.create(name: String, description: String?, ownerId: UUID): ProjectResponse

    /**
     * Finds a project by id.
     *
     * @param id the id of the project
     * @return the project [ProjectResponse] with the given id, or null if no such project exists
     */
    suspend fun Transaction.findById(id: UUID): ProjectResponse?

    /**
     * Finds all projects owned by the given user with pagination.
     *
     * @param ownerId the id of the user
     * @param page the page number (0-based)
     * @param size the page size
     * @return a paginated response of projects [ProjectResponse] owned by the user
     */
    suspend fun Transaction.findByOwner(
        ownerId: UUID,
        page: Int = 0,
        size: Int = 10
    ): PaginatedResponse<ProjectResponse>

    /**
     * Finds all projects with pagination.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @return a paginated response of all projects [ProjectResponse]
     */
    suspend fun Transaction.findAll(page: Int = 0, size: Int = 10): PaginatedResponse<ProjectResponse>

    /**
     * Updates an existing project.
     *
     * @param id the id of the project to update
     * @param name the new name of the project
     * @param description the new description of the project
     * @return true if the project was updated, false otherwise
     */
    suspend fun Transaction.update(id: UUID, name: String, description: String?): Boolean

    /**
     * Deletes a project by id.
     *
     * @param id the id of the project to delete
     * @return true if the project was deleted, false otherwise
     */
    suspend fun Transaction.delete(id: UUID): Boolean
}
