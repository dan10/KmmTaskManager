package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.getTestModule
import com.danioliveira.taskmanager.routes.toUUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ProjectServiceTest : KoinTest {
    private val projectService: ProjectService by inject()

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()

        // Start Koin with the test module
        startKoin {
            modules(getTestModule())
        }
        Unit
    }

    @After
    fun tearDown() = runBlocking {
        // Clear the database
        TestDatabase.clearDatabase()

        // Stop Koin
        stopKoin()
    }

    @Test
    fun `test get projects by owner`() = runTest {
        // Create an owner in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner1@example.com",
            displayName = "Owner 1"
        ))

        // Create projects for the owner
        val request1 = ProjectCreateRequest("Project 1", "Description 1")
        val project1 = projectService.createProject(ownerId, request1)

        val request2 = ProjectCreateRequest("Project 2", "Description 2")
        val project2 = projectService.createProject(ownerId, request2)

        // Create a project for another owner
        val otherOwnerId = UUID.fromString(createTestUser(
            email = "owner2@example.com",
            displayName = "Owner 2"
        ))
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
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

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
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Get the project by ID
        val foundProject = projectService.getProjectById(project.id.toUUID(), ownerId)

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
            projectService.getProjectById(UUID.randomUUID(), UUID.randomUUID())
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Project"))
        }
    }

    @Test
    fun `test update project`() = runTest {
        // Create a user in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val createRequest = ProjectCreateRequest("Original Name", "Original Description")
        val project = projectService.createProject(ownerId, createRequest)

        // Create an update request
        val updateRequest = ProjectUpdateRequest(
            name = "Updated Name",
            description = "Updated Description"
        )

        // Update the project
        val updated = projectService.updateProjectWithPermission(project.id, ownerId, updateRequest)

        // Verify the update was successful
        assertTrue(updated)

        // Get the project to verify the changes
        val updatedProject = projectService.getProjectById(project.id.toUUID(), ownerId)
        assertEquals(updateRequest.name, updatedProject.name)
        assertEquals(updateRequest.description, updatedProject.description)
    }

    @Test
    fun `test delete project`() = runTest {
        // Create a user in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Project to Delete", "This project will be deleted")
        val project = projectService.createProject(ownerId, request)

        // Delete the project
        val deleted = projectService.deleteProjectWithPermission(project.id.toUUID(), ownerId)

        // Verify the deletion was successful
        assertTrue(deleted)

        // Try to get the project to verify it was deleted
        try {
            projectService.getProjectById(project.id.toUUID(), ownerId)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Project"))
        }
    }

    @Test
    fun `test assign user to project`() = runTest {
        // Create a project owner in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = UUID.fromString(createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ))

        // Assign the user to the project
        val assignment = projectService.assignUserToProject(project.id.toUUID(), userId)

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
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = UUID.fromString(createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ))

        // Assign the user to the project
        projectService.assignUserToProject(project.id.toUUID(), userId)

        // Verify the user is assigned to the project
        assertTrue(projectService.isUserAssignedToProject(project.id, userId.toString()))

        // Remove the user from the project
        val removed = projectService.removeUserFromProject(project.id.toUUID(), userId)

        // Verify the removal was successful
        assertTrue(removed)

        // Verify the user is no longer assigned to the project
        assertFalse(projectService.isUserAssignedToProject(project.id, userId.toString()))
    }

    @Test
    fun `test get users by project`() = runTest {
        // Create a project owner in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create users to assign to the project
        val user1Id = UUID.fromString(createTestUser(
            email = "user1@example.com",
            displayName = "Project User 1"
        ))
        val user2Id = UUID.fromString(createTestUser(
            email = "user2@example.com",
            displayName = "Project User 2"
        ))

        // Assign users to the project
        projectService.assignUserToProject(project.id.toUUID(), user1Id)
        projectService.assignUserToProject(project.id.toUUID(), user2Id)

        // Get users assigned to the project
        val users = projectService.getUsersByProject(project.id)

        // Verify the correct users were returned
        assertEquals(2, users.size)
        assertTrue(users.contains(user1Id.toString()))
        assertTrue(users.contains(user2Id.toString()))
    }

    @Test
    fun `test get projects by user`() = runTest {
        // Create project owners in the database
        val owner1Id = UUID.fromString(createTestUser(
            email = "owner1@example.com",
            displayName = "Project Owner 1"
        ))
        val owner2Id = UUID.fromString(createTestUser(
            email = "owner2@example.com",
            displayName = "Project Owner 2"
        ))

        // Create projects
        val request1 = ProjectCreateRequest("Project 1", "Description 1")
        val project1 = projectService.createProject(owner1Id, request1)

        val request2 = ProjectCreateRequest("Project 2", "Description 2")
        val project2 = projectService.createProject(owner2Id, request2)

        // Create a user to assign to the projects
        val userId = UUID.fromString(createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ))

        // Assign the user to the projects
        projectService.assignUserToProject(project1.id.toUUID(), userId)
        projectService.assignUserToProject(project2.id.toUUID(), userId)

        // Get projects the user is assigned to
        val projects = projectService.getProjectsByUser(userId.toString())

        // Verify the correct projects were returned
        assertEquals(2, projects.size)
        assertTrue(projects.contains(project1.id))
        assertTrue(projects.contains(project2.id))
    }

    @Test
    fun `test is user assigned to project`() = runTest {
        // Create a project owner in the database
        val ownerId = UUID.fromString(createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        ))

        // Create a project
        val request = ProjectCreateRequest("Test Project", "Test Description")
        val project = projectService.createProject(ownerId, request)

        // Create a user to assign to the project
        val userId = UUID.fromString(createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        ))

        // Assign the user to the project
        projectService.assignUserToProject(project.id.toUUID(), userId)

        // Check if the user is assigned to the project
        val isAssigned = projectService.isUserAssignedToProject(project.id, userId.toString())

        // Verify the user is assigned
        assertTrue(isAssigned)

        // Create another user
        val otherUserId = UUID.fromString(createTestUser(
            email = "otheruser@example.com",
            displayName = "Other Project User"
        ))

        // Check if the other user is assigned to the project
        val otherIsAssigned = projectService.isUserAssignedToProject(project.id, otherUserId.toString())

        // Verify the other user is not assigned
        assertFalse(otherIsAssigned)
    }
}
