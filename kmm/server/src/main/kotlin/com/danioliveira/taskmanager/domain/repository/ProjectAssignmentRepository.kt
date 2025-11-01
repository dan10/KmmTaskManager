package com.danioliveira.taskmanager.domain.repository

import com.danioliveira.taskmanager.domain.ProjectAssignment
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ProjectAssignmentRepository {
    /**
     * Assigns a user to a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return the newly created project assignment
     */
    context(transaction: R2dbcTransaction)
    suspend fun assignUserToProject(projectId: Uuid, userId: Uuid): ProjectAssignment

    /**
     * Removes a user from a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return true if the user was removed, false otherwise
     */
    context(transaction: R2dbcTransaction)
    suspend fun removeUserFromProject(projectId: Uuid, userId: Uuid): Boolean

    /**
     * Finds all users assigned to a project.
     *
     * @param projectId the id of the project
     * @return a list of user IDs assigned to the project
     */
    context(transaction: R2dbcTransaction)
    suspend fun findUsersByProject(projectId: Uuid): List<Uuid>

    /**
     * Finds all projects a user is assigned to.
     *
     * @param userId the id of the user
     * @return a list of project IDs the user is assigned to
     */
    context(transaction: R2dbcTransaction)
    suspend fun findProjectsByUser(userId: Uuid): List<Uuid>

    /**
     * Checks if a user is assigned to a project.
     *
     * @param projectId the id of the project
     * @param userId the id of the user
     * @return true if the user is assigned to the project, false otherwise
     */
    context(transaction: R2dbcTransaction)
    suspend fun isUserAssignedToProject(projectId: Uuid, userId: Uuid): Boolean
}