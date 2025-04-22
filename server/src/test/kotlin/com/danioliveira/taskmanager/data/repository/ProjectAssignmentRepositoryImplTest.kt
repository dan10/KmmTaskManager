package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.*

class ProjectAssignmentRepositoryImplTest {
    private lateinit var assignmentRepository: ProjectAssignmentRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var testUserId: UUID
    private lateinit var testProjectId: UUID

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()
        assignmentRepository = ProjectAssignmentRepositoryImpl()
        userRepository = UserRepositoryImpl()
        projectRepository = ProjectRepositoryImpl()

        // Create a test user
        val user = dbQuery {
            with(userRepository) {
                create("test@example.com", "password", "Test User", null)
            }
        }
        testUserId = UUID.fromString(user.id)

        // Create a test project
        val project = dbQuery {
            with(projectRepository) {
                create("Test Project", "Test Project Description", testUserId)
            }
        }
        testProjectId = UUID.fromString(project.id)
    }

    @After
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()
    }

    @Test
    fun `test assign user to project`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Assign the second user to the project
        val assignment = dbQuery {
            with(assignmentRepository) {
                assignUserToProject(testProjectId, secondUserId)
            }
        }

        // Verify the assignment was created correctly
        assertNotNull(assignment)
        assertEquals(testProjectId.toString(), assignment.projectId)
        assertEquals(secondUserId.toString(), assignment.userId)

        // Verify the user is now assigned to the project
        val isAssigned = dbQuery {
            with(assignmentRepository) {
                isUserAssignedToProject(testProjectId, secondUserId)
            }
        }
        assertTrue(isAssigned)
    }

    @Test
    fun `test assign user to project - already assigned`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Assign the second user to the project
        dbQuery {
            with(assignmentRepository) {
                assignUserToProject(testProjectId, secondUserId)
            }
        }

        // Try to assign the user to the same project again
        // This should throw an IllegalStateException
        try {
            dbQuery {
                with(assignmentRepository) {
                    assignUserToProject(testProjectId, secondUserId)
                }
            }
            fail("Expected IllegalStateException was not thrown")
        } catch (e: IllegalStateException) {
            // Expected exception
            assertTrue(e.message?.contains("already assigned") == true)
        }
    }

    @Test
    fun `test remove user from project`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Assign the second user to the project
        dbQuery {
            with(assignmentRepository) {
                assignUserToProject(testProjectId, secondUserId)
            }
        }

        // Verify the user is assigned to the project
        val isAssigned = dbQuery {
            with(assignmentRepository) {
                isUserAssignedToProject(testProjectId, secondUserId)
            }
        }
        assertTrue(isAssigned)

        // Remove the user from the project
        val removed = dbQuery {
            with(assignmentRepository) {
                removeUserFromProject(testProjectId, secondUserId)
            }
        }

        // Verify the user was removed
        assertTrue(removed)

        // Verify the user is no longer assigned to the project
        val isStillAssigned = dbQuery {
            with(assignmentRepository) {
                isUserAssignedToProject(testProjectId, secondUserId)
            }
        }
        assertFalse(isStillAssigned)
    }

    @Test
    fun `test remove user from project - not assigned`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Try to remove a user that is not assigned to the project
        val removed = dbQuery {
            with(assignmentRepository) {
                removeUserFromProject(testProjectId, secondUserId)
            }
        }

        // Verify the removal failed
        assertFalse(removed)
    }

    @Test
    fun `test find users by project`() = runBlocking {
        // Create multiple users
        val user1 = dbQuery {
            with(userRepository) {
                create("user1@example.com", "password", "User 1", null)
            }
        }
        val user1Id = UUID.fromString(user1.id)

        val user2 = dbQuery {
            with(userRepository) {
                create("user2@example.com", "password", "User 2", null)
            }
        }
        val user2Id = UUID.fromString(user2.id)

        val user3 = dbQuery {
            with(userRepository) {
                create("user3@example.com", "password", "User 3", null)
            }
        }
        val user3Id = UUID.fromString(user3.id)

        // Assign users 1 and 2 to the project
        dbQuery {
            with(assignmentRepository) {
                assignUserToProject(testProjectId, user1Id)
                assignUserToProject(testProjectId, user2Id)
            }
        }

        // Find users assigned to the project
        val users = dbQuery {
            with(assignmentRepository) {
                findUsersByProject(testProjectId)
            }
        }

        // Verify the correct users were found
        assertEquals(2, users.size)
        assertTrue(users.contains(user1Id))
        assertTrue(users.contains(user2Id))
        assertFalse(users.contains(user3Id))
    }

    @Test
    fun `test find projects by user`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create multiple projects
        val project1 = dbQuery {
            with(projectRepository) {
                create("Project 1", "Description 1", testUserId)
            }
        }
        val project1Id = UUID.fromString(project1.id)

        val project2 = dbQuery {
            with(projectRepository) {
                create("Project 2", "Description 2", testUserId)
            }
        }
        val project2Id = UUID.fromString(project2.id)

        // Assign the second user to projects 1 and 2
        dbQuery {
            with(assignmentRepository) {
                assignUserToProject(project1Id, secondUserId)
                assignUserToProject(project2Id, secondUserId)
            }
        }

        // Find projects the second user is assigned to
        val projects = dbQuery {
            with(assignmentRepository) {
                findProjectsByUser(secondUserId)
            }
        }

        // Verify the correct projects were found
        assertEquals(2, projects.size)
        assertTrue(projects.contains(project1Id))
        assertTrue(projects.contains(project2Id))
    }

    @Test
    fun `test is user assigned to project`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Check if the user is assigned to the project (should be false)
        val isAssignedBefore = dbQuery {
            with(assignmentRepository) {
                isUserAssignedToProject(testProjectId, secondUserId)
            }
        }
        assertFalse(isAssignedBefore)

        // Assign the user to the project
        dbQuery {
            with(assignmentRepository) {
                assignUserToProject(testProjectId, secondUserId)
            }
        }

        // Check if the user is assigned to the project (should be true)
        val isAssignedAfter = dbQuery {
            with(assignmentRepository) {
                isUserAssignedToProject(testProjectId, secondUserId)
            }
        }
        assertTrue(isAssignedAfter)
    }
}