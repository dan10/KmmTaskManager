package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.getTestModule
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDateTime
import java.util.*
import kotlin.test.*

class TaskServiceTest : KoinTest {
    private val taskService: TaskService by inject()
    private val projectService: ProjectService by inject()

    @Before
    fun setUp() {
        // Initialize the H2 database
        TestDatabase.init()

        // Start Koin with the test module
        startKoin {
            modules(getTestModule(ApplicationConfig("application_test.conf")))
        }
    }

    @After
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()

        // Stop Koin
        stopKoin()
    }

    @Test
    fun `test find all tasks`() = runBlocking {
        // Create users in the database
        val creatorId = createTestUser(
            email = "creator@example.com",
            displayName = "Task Creator"
        )
        val assigneeId = createTestUser(
            email = "assignee@example.com",
            displayName = "Task Assignee"
        )

        // Create an actual project in the database
        val projectResponse = projectService.createProject(
            creatorId, ProjectCreateRequest(
                name = "Test Project for Tasks",
                description = "Project for testing task listing"
            )
        )
        val projectId = projectResponse.id

        // Create task 1
        val request1 = TaskCreateRequest(
            title = "Task 1",
            description = "Description 1",
            projectId = projectId,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task1 = taskService.create(request1, creatorId)

        // Create task 2
        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = projectId,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task2 = taskService.create(request2, creatorId)

        // Create task 3 (no project)
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task3 = taskService.create(request3, creatorId)

        // Find all tasks for the project
        val projectTasks = taskService.findAll(projectId)

        // Verify the correct tasks were returned
        assertEquals(2, projectTasks.total)
        assertEquals(2, projectTasks.items.size)
        assertTrue(projectTasks.items.any { it.id == task1.id })
        assertTrue(projectTasks.items.any { it.id == task2.id })
        assertFalse(projectTasks.items.any { it.id == task3.id })

        // Find all tasks
        val allTasks = taskService.findAll(null)

        // Verify all tasks were returned
        assertEquals(3, allTasks.total)
        assertEquals(3, allTasks.items.size)
        assertTrue(allTasks.items.any { it.id == task1.id })
        assertTrue(allTasks.items.any { it.id == task2.id })
        assertTrue(allTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find tasks by owner`() = runBlocking {
        // Create actual users in the database
        val owner1Id = createTestUser(email = "owner1@example.com", displayName = "Task Owner 1")
        val owner2Id = createTestUser(email = "owner2@example.com", displayName = "Task Owner 2")
        val assigneeId =
            createTestUser(email = "assignee_owner_test@example.com", displayName = "Task Assignee Owner Test")

        // Create tasks for owner1
        val request1 = TaskCreateRequest(
            title = "Task 1",
            description = "Description 1",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task1 = taskService.create(request1, owner1Id)

        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task2 = taskService.create(request2, owner1Id)

        // Create task for owner2
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task3 = taskService.create(request3, owner2Id)

        // Find tasks by owner1
        val owner1Tasks = taskService.findAllByOwnerId(owner1Id)

        // Verify the correct tasks were returned
        assertEquals(2, owner1Tasks.total)
        assertEquals(2, owner1Tasks.items.size)
        assertTrue(owner1Tasks.items.any { it.id == task1.id })
        assertTrue(owner1Tasks.items.any { it.id == task2.id })
        assertFalse(owner1Tasks.items.any { it.id == task3.id })

        // Find tasks by owner2
        val owner2Tasks = taskService.findAllByOwnerId(owner2Id)

        // Verify the correct tasks were returned
        assertEquals(1, owner2Tasks.total)
        assertEquals(1, owner2Tasks.items.size)
        assertTrue(owner2Tasks.items.any { it.id == task3.id })
        assertFalse(owner2Tasks.items.any { it.id == task1.id })
        assertFalse(owner2Tasks.items.any { it.id == task2.id })
    }

    @Test
    fun `test find tasks by assignee`() = runBlocking {
        // Create actual users in the database
        val assignee1Id = createTestUser(email = "assignee1@example.com", displayName = "Task Assignee 1")
        val assignee2Id = createTestUser(email = "assignee2@example.com", displayName = "Task Assignee 2")
        val creatorId =
            createTestUser(email = "creator_assignee_test@example.com", displayName = "Task Creator Assignee Test")

        // Create tasks for assignee1
        val request1 = TaskCreateRequest(
            title = "Task 1",
            description = "Description 1",
            projectId = null,
            assigneeId = assignee1Id,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task1 = taskService.create(request1, creatorId)

        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = null,
            assigneeId = assignee1Id,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task2 = taskService.create(request2, creatorId)

        // Create task for assignee2
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assignee2Id,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task3 = taskService.create(request3, creatorId)

        // Find tasks by assignee1
        val assignee1Tasks = taskService.findAllByAssigneeId(assignee1Id)

        // Verify the correct tasks were returned
        assertEquals(2, assignee1Tasks.total)
        assertEquals(2, assignee1Tasks.items.size)
        assertTrue(assignee1Tasks.items.any { it.id == task1.id })
        assertTrue(assignee1Tasks.items.any { it.id == task2.id })
        assertFalse(assignee1Tasks.items.any { it.id == task3.id })

        // Find tasks by assignee2
        val assignee2Tasks = taskService.findAllByAssigneeId(assignee2Id)

        // Verify the correct tasks were returned
        assertEquals(1, assignee2Tasks.total)
        assertEquals(1, assignee2Tasks.items.size)
        assertTrue(assignee2Tasks.items.any { it.id == task3.id })
        assertFalse(assignee2Tasks.items.any { it.id == task1.id })
        assertFalse(assignee2Tasks.items.any { it.id == task2.id })
    }

    @Test
    fun `test find task by id`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator_find@example.com", displayName = "Task Creator Find")
        val assigneeId = createTestUser(email = "assignee_find@example.com", displayName = "Task Assignee Find")

        // Create a task
        val request = TaskCreateRequest(
            title = "Test Task",
            description = "Test Description",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Find the task by ID
        val foundTask = taskService.findById(task.id)

        // Verify the correct task was returned
        assertNotNull(foundTask)
        assertEquals(task.id, foundTask.id)
        assertEquals(task.title, foundTask.title)
        assertEquals(task.description, foundTask.description)
    }

    @Test
    fun `test find task by id - not found`() = runBlocking {
        // Try to find a task that doesn't exist
        try {
            taskService.findById(UUID.randomUUID().toString())
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }

    @Test
    fun `test create task`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator@example.com", displayName = "Task Creator")
        val assigneeId = createTestUser(email = "assignee@example.com", displayName = "Task Assignee")

        // Create an actual project in the database
        val projectResponse = projectService.createProject(
            creatorId, ProjectCreateRequest(
                name = "Test Project",
                description = "Test Project Description"
            )
        )
        val projectId = projectResponse.id

        val dueDate = LocalDateTime.now().plusDays(7).toString()
        val request = TaskCreateRequest(
            title = "New Task",
            description = "New Task Description",
            projectId = projectId,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = dueDate
        )

        // Create the task
        val task = taskService.create(request, creatorId)

        // Verify the task was created correctly
        assertNotNull(task)
        assertEquals(request.title, task.title)
        assertEquals(request.description, task.description)
        assertEquals(request.projectId, task.projectId)
        assertEquals(request.assigneeId, task.assigneeId)
        assertEquals(creatorId, task.creatorId)
        assertEquals(TaskStatus.TODO, task.status)
    }

    @Test
    fun `test update task`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator2@example.com", displayName = "Task Creator 2")
        val assigneeId = createTestUser(email = "assignee2@example.com", displayName = "Task Assignee 2")

        // Create a task
        val createRequest = TaskCreateRequest(
            title = "Original Title",
            description = "Original Description",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task = taskService.create(createRequest, creatorId)

        // Create a new user for assignment
        val newAssigneeId = createTestUser(email = "newassignee@example.com", displayName = "New Task Assignee")
        val newDueDate = LocalDateTime.now().plusDays(14).toString()
        val updateRequest = TaskUpdateRequest(
            title = "Updated Title",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS,
            dueDate = newDueDate,
            assigneeId = newAssigneeId
        )

        // Update the task
        val updatedTask = taskService.update(task.id, updateRequest)

        // Verify the task was updated correctly
        assertNotNull(updatedTask)
        assertEquals(updateRequest.title, updatedTask.title)
        assertEquals(updateRequest.description, updatedTask.description)
        assertEquals(updateRequest.status, updatedTask.status)
        assertEquals(updateRequest.dueDate, updatedTask.dueDate)
        assertEquals(updateRequest.assigneeId, updatedTask.assigneeId)
    }

    @Test
    fun `test delete task`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator3@example.com", displayName = "Task Creator 3")
        val assigneeId = createTestUser(email = "assignee3@example.com", displayName = "Task Assignee 3")

        // Create a task
        val request = TaskCreateRequest(
            title = "Task to Delete",
            description = "This task will be deleted",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Delete the task
        val deleted = taskService.delete(task.id)

        // Verify the deletion was successful
        assertTrue(deleted)

        // Try to find the task to verify it was deleted
        try {
            taskService.findById(task.id)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }

    @Test
    fun `test assign task`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator4@example.com", displayName = "Task Creator 4")
        val assigneeId = createTestUser(email = "assignee4@example.com", displayName = "Task Assignee 4")

        // Create a task
        val request = TaskCreateRequest(
            title = "Test Task",
            description = "Test Description",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Create a new user and assign the task to them
        val newAssigneeId = createTestUser(email = "newassignee2@example.com", displayName = "New Task Assignee 2")
        val updatedTask = taskService.assign(task.id, newAssigneeId)

        // Verify the task was assigned correctly
        assertNotNull(updatedTask)
        assertEquals(newAssigneeId, updatedTask.assigneeId)
    }

    @Test
    fun `test change task status`() = runBlocking {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator5@example.com", displayName = "Task Creator 5")
        val assigneeId = createTestUser(email = "assignee5@example.com", displayName = "Task Assignee 5")

        // Create a task
        val request = TaskCreateRequest(
            title = "Test Task",
            description = "Test Description",
            projectId = null,
            assigneeId = assigneeId,
            status = TaskStatus.TODO.name,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Change the task status
        val updatedTask = taskService.changeStatus(task.id, TaskStatus.IN_PROGRESS.name)

        // Verify the status was changed correctly
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status)
    }
}
