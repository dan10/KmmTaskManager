package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.ProjectAssignment
import org.jetbrains.exposed.sql.Transaction
import java.util.*

interface ProjectAssignmentRepository {
    /**
     * Assigns a user to a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return the newly created project assignment
     */
    suspend fun Transaction.assignUserToProject(projectId: UUID, userId: UUID): ProjectAssignment

    /**
     * Removes a user from a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return true if the user was removed, false otherwise
     */
    suspend fun Transaction.removeUserFromProject(projectId: UUID, userId: UUID): Boolean

    /**
     * Finds all users assigned to a project.
     *
     * @param projectId the id of the project
     * @return a list of user IDs assigned to the project
     */
    suspend fun Transaction.findUsersByProject(projectId: UUID): List<UUID>

    /**
     * Finds all projects a user is assigned to.
     *
     * @param userId the id of the user
     * @return a list of project IDs the user is assigned to
     */
    suspend fun Transaction.findProjectsByUser(userId: UUID): List<UUID>

    /**
     * Checks if a user is assigned to a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return true if the user is assigned to the project, false otherwise
     */
    suspend fun Transaction.isUserAssignedToProject(projectId: UUID, userId: UUID): Boolean
}