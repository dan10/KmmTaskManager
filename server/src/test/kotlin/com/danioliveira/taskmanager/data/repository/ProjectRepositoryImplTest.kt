package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.*

class ProjectRepositoryImplTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var userRepository: UserRepository
    private lateinit var testUserId: UUID

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()
        projectRepository = ProjectRepositoryImpl()
        userRepository = UserRepositoryImpl()

        // Create a test user to be used as the owner in project tests
        val user = dbQuery {
            with(userRepository) {
                create("test@example.com", "password", "Test User", null)
            }
        }
        testUserId = UUID.fromString(user.id)
    }

    @After
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()
    }

    @Test
    fun `test create and find project by id`() = runBlocking {
        // Create a project
        val name = "Test Project"
        val description = "This is a test project"

        val project = dbQuery {
            with(projectRepository) {
                create(name, description, testUserId)
            }
        }

        // Verify the project was created correctly
        assertNotNull(project)
        assertEquals(name, project.name)
        assertEquals(description, project.description)
        assertEquals(testUserId.toString(), project.ownerId)

        // Find the project by ID
        val foundProject = dbQuery {
            with(projectRepository) {
                findById(UUID.fromString(project.id))
            }
        }

        // Verify the project was found
        assertNotNull(foundProject)
        assertEquals(project.id, foundProject.id)
        assertEquals(name, foundProject.name)
        assertEquals(description, foundProject.description)
    }

    @Test
    fun `test find projects by owner`() = runBlocking {
        // Create two projects for the test user
        val project1 = dbQuery {
            with(projectRepository) {
                create("Project 1", "Description 1", testUserId)
            }
        }

        val project2 = dbQuery {
            with(projectRepository) {
                create("Project 2", "Description 2", testUserId)
            }
        }

        // Create another user and a project for them
        val otherUser = dbQuery {
            with(userRepository) {
                create("other@example.com", "password", "Other User", null)
            }
        }
        val otherUserId = UUID.fromString(otherUser.id)

        dbQuery {
            with(projectRepository) {
                create("Other Project", "Other Description", otherUserId)
            }
        }

        // Find projects by owner
        val projects = dbQuery {
            with(projectRepository) {
                findByOwner(testUserId, 0, 10)
            }
        }

        // Verify the correct projects were found
        assertEquals(2, projects.total)
        assertEquals(2, projects.items.size)
        assertTrue(projects.items.any { it.id == project1.id })
        assertTrue(projects.items.any { it.id == project2.id })
    }

    @Test
    fun `test find all projects with pagination`() = runBlocking {
        // Create multiple projects
        val projects = mutableListOf<ProjectResponse>()

        // Create 15 projects (more than one page with size 10)
        for (i in 1..15) {
            val project = dbQuery {
                with(projectRepository) {
                    create("Project $i", "Description $i", testUserId)
                }
            }
            projects.add(project)
        }

        // Get first page (10 items)
        val page1 = dbQuery {
            with(projectRepository) {
                findAll(0, 10)
            }
        }

        // Verify pagination works correctly
        assertEquals(15, page1.total)
        assertEquals(10, page1.items.size)
        assertEquals(0, page1.page)
        assertEquals(10, page1.size)
        assertEquals(2, page1.totalPages)

        // Get second page (5 items)
        val page2 = dbQuery {
            with(projectRepository) {
                findAll(1, 10)
            }
        }

        // Verify second page
        assertEquals(15, page2.total)
        assertEquals(5, page2.items.size)
        assertEquals(1, page2.page)
        assertEquals(10, page2.size)
        assertEquals(2, page2.totalPages)
    }

    @Test
    fun `test update project`() = runBlocking {
        // Create a project
        val project = dbQuery {
            with(projectRepository) {
                create("Original Name", "Original Description", testUserId)
            }
        }

        // Update the project
        val updated = dbQuery {
            with(projectRepository) {
                update(UUID.fromString(project.id), "Updated Name", "Updated Description")
            }
        }

        // Verify the update was successful
        assertTrue(updated)

        // Find the project to verify the changes
        val updatedProject = dbQuery {
            with(projectRepository) {
                findById(UUID.fromString(project.id))
            }
        }

        // Verify the project was updated correctly
        assertNotNull(updatedProject)
        assertEquals("Updated Name", updatedProject.name)
        assertEquals("Updated Description", updatedProject.description)
    }

    @Test
    fun `test delete project`() = runBlocking {
        // Create a project
        val project = dbQuery {
            with(projectRepository) {
                create("Project to Delete", "This project will be deleted", testUserId)
            }
        }

        // Verify the project exists
        val foundProject = dbQuery {
            with(projectRepository) {
                findById(UUID.fromString(project.id))
            }
        }
        assertNotNull(foundProject)

        // Delete the project
        val deleted = dbQuery {
            with(projectRepository) {
                delete(UUID.fromString(project.id))
            }
        }

        // Verify the deletion was successful
        assertTrue(deleted)

        // Verify the project no longer exists
        val deletedProject = dbQuery {
            with(projectRepository) {
                findById(UUID.fromString(project.id))
            }
        }
        assertNull(deletedProject)
    }

    @Test
    fun `test update non-existent project`() = runBlocking {
        // Try to update a project that doesn't exist
        val updated = dbQuery {
            with(projectRepository) {
                update(UUID.randomUUID(), "Updated Name", "Updated Description")
            }
        }

        // Verify the update failed
        assertFalse(updated)
    }

    @Test
    fun `test delete non-existent project`() = runBlocking {
        // Try to delete a project that doesn't exist
        val deleted = dbQuery {
            with(projectRepository) {
                delete(UUID.randomUUID())
            }
        }

        // Verify the deletion failed
        assertFalse(deleted)
    }
}