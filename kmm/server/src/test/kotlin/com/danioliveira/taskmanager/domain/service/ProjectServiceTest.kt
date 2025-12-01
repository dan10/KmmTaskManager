package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.BaseServiceTest
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import com.danioliveira.taskmanager.utils.toUuid
import com.danioliveira.taskmanager.utils.randomV7

@OptIn(ExperimentalUuidApi::class)
class ProjectServiceTest : BaseServiceTest() {
    private val projectService: ProjectService by inject()

    @Test
    fun `test get projects by owner`() = runTest {
        // Create an owner in the database
        val ownerId = createTestUser(
            email = "owner1@example.com",
            displayName = "Owner 1"
        ).toUuid()

        // Create projects for the owner
        val request1 = ProjectCreateRequest("Project 1", "Description 1")
        val project1 = projectService.createProject(ownerId, request1)

        val request2 = ProjectCreateRequest("Project 2", "Description 2")
        val project2 = projectService.createProject(ownerId, request2)

        // Create a project for another owner
        val otherOwnerId = createTestUser(
            email = "owner2@example.com",
            displayName = "Owner 2"
        ).toUuid()
        val request3 = ProjectCreateRequest("Project 3", "Description 3")
        projectService.createProject(otherOwnerId, request3)

        // Get projects by owner
        val projects = projectService.getProjectsByOwner(ownerId)

        // Verify the correct projects were returned
        assertEquals(2, projects.total)
        assertEquals(2, projects.items.size)
        assertTrue(projects.items.any { it.id == project1.id })
        assertTrue(projects.items.any { it.id == project2.id })
    }


    @Test
    fun `test create project`() = runTest {
        // Create a user in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a request
        val request = ProjectCreateRequest(
            name = "New Project",
            description = "New Project Description"
        )

        // Create the project
        val project = projectService.createProject(ownerId, request)

        // Verify the project was created correctly
        assertNotNull(project)
        assertEquals(request.name, project.name)
        assertEquals(request.description, project.description)
        assertEquals(ownerId.toString(), project.ownerId)
    }

    @Test
    fun `test get project by id`() = runTest {
        // Create a user in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Get the project by ID
        val foundProject = projectService.getProjectById(project.id.toUuid(), ownerId)

        // Verify the correct project was returned
        assertNotNull(foundProject)
        assertEquals(project.id, foundProject.id)
        assertEquals(project.name, foundProject.name)
        assertEquals(project.description, foundProject.description)
    }

    @Test
    fun `test get project by id - not found`() = runTest {
        // Try to get a project that doesn't exist
        try {
            projectService.getProjectById(Uuid.randomV7(), Uuid.randomV7())
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Project"))
        }
    }

    @Test
    fun `test update project`() = runTest {
        // Create a user in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val createRequest = ProjectCreateRequest("Original Name", "Original Description")
        val project = projectService.createProject(ownerId, createRequest)

        // Create an update request
        val updateRequest = ProjectUpdateRequest(
            name = "Updated Name",
            description = "Updated Description"
        )

        // Update the project
        val updated = projectService.updateProject(project.id.toUuid(), ownerId, updateRequest)

        // Verify the update was successful
        assertTrue(updated)

        // Get the project to verify the changes
        val updatedProject = projectService.getProjectById(project.id.toUuid(), ownerId)
        assertEquals(updateRequest.name, updatedProject.name)
        assertEquals(updateRequest.description, updatedProject.description)
    }

    @Test
    fun `test delete project`() = runTest {
        // Create a user in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Project to Delete", "This project will be deleted")
        val project = projectService.createProject(ownerId, request)

        // Delete the project
        val deleted = projectService.deleteProject(project.id.toUuid(), ownerId)

        // Verify the deletion was successful
        assertTrue(deleted)

        // Try to get the project to verify it was deleted
        try {
            projectService.getProjectById(project.id.toUuid(), ownerId)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Project"))
        }
    }

    @Test
    fun `test assign user to project`() = runTest {
        // Create a project owner in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ).toUuid()

        // Assign the user to the project
        val assignment = projectService.assignUserToProject(project.id.toUuid(), userId, ownerId)

        // Verify the assignment was created correctly
        assertNotNull(assignment)
        assertEquals(project.id, assignment.projectId)
        assertEquals(userId.toString(), assignment.userId)

        // Verify the user is assigned to the project
        assertTrue(projectService.isUserAssignedToProject(project.id, userId.toString()))
    }

    @Test
    fun `test remove user from project`() = runTest {
        // Create a project owner in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ).toUuid()

        // Assign the user to the project
        projectService.assignUserToProject(project.id.toUuid(), userId, ownerId)

        // Verify the user is assigned to the project
        assertTrue(projectService.isUserAssignedToProject(project.id, userId.toString()))

        // Remove the user from the project
        val removed = projectService.removeUserFromProject(project.id.toUuid(), userId, ownerId)

        // Verify the removal was successful
        assertTrue(removed)

        // Verify the user is no longer assigned to the project
        assertFalse(projectService.isUserAssignedToProject(project.id, userId.toString()))
    }

    @Test
    fun `test get users by project`() = runTest {
        // Create a project owner in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create users to assign to the project
        val user1Id = createTestUser(
            email = "user1@example.com",
            displayName = "Project User 1"
        ).toUuid()
        val user2Id = createTestUser(
            email = "user2@example.com",
            displayName = "Project User 2"
        ).toUuid()

        // Assign users to the project
        projectService.assignUserToProject(project.id.toUuid(), user1Id, ownerId)
        projectService.assignUserToProject(project.id.toUuid(), user2Id, ownerId)

        // Get users assigned to the project
        val users = projectService.getUsersByProject(project.id.toUuid())

        // Verify the correct users were returned
        assertEquals(2, users.size)
        assertTrue(users.contains(user1Id.toString()))
        assertTrue(users.contains(user2Id.toString()))
    }

    @Test
    fun `test get projects by user`() = runTest {
        // Create project owners in the database
        val owner1Id = createTestUser(
            email = "owner1@example.com",
            displayName = "Project Owner 1"
        ).toUuid()
        val owner2Id = createTestUser(
            email = "owner2@example.com",
            displayName = "Project Owner 2"
        ).toUuid()

        // Create projects
        val request1 = ProjectCreateRequest("Project 1", "Description 1")
        val project1 = projectService.createProject(owner1Id, request1)

        val request2 = ProjectCreateRequest("Project 2", "Description 2")
        val project2 = projectService.createProject(owner2Id, request2)

        // Create a user to assign to the projects
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ).toUuid()

        // Assign the user to the projects
        projectService.assignUserToProject(project1.id.toUuid(), userId, owner1Id)
        projectService.assignUserToProject(project2.id.toUuid(), userId, owner2Id)

        // Get projects the user is assigned to
        val projects = projectService.getProjectsByUser(userId)

        // Verify the correct projects were returned
        assertEquals(2, projects.size)
        assertTrue(projects.contains(project1.id))
        assertTrue(projects.contains(project2.id))
    }

    @Test
    fun `test is user assigned to project`() = runTest {
        // Create a project owner in the database
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ).toUuid()

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ).toUuid()

        // Assign the user to the project
        projectService.assignUserToProject(project.id.toUuid(), userId, ownerId)

        // Check if the user is assigned to the project
        val isAssigned = projectService.isUserAssignedToProject(project.id, userId.toString())

        // Verify the user is assigned
        assertTrue(isAssigned)

        // Create another user
        val otherUserId = createTestUser(
            email = "otheruser@example.com",
            displayName = "Other Project User"
        ).toUuid()

        // Check if the other user is assigned to the project
        val otherIsAssigned = projectService.isUserAssignedToProject(project.id, otherUserId.toString())

        // Verify the other user is not assigned
        assertFalse(otherIsAssigned)
    }
}
