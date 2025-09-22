package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.BaseServiceTest
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.exceptions.ForbiddenException
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.routes.toUUID
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import org.koin.test.inject
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Clock

class TaskServiceTest : BaseServiceTest() {
    private val taskService: TaskService by inject()
    private val projectService: ProjectService by inject()

    @Test
    fun `test find all tasks by project`() = runTest {
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
            creatorId.toUUID(), ProjectCreateRequest(
                name = "Test Project for Tasks",
                description = "Project for testing task listing"
            )
        )
        val projectId = projectResponse.id

        // Assign the assignee to the project
        projectService.assignUserToProject(projectId.toUUID(), assigneeId.toUUID(), creatorId.toUUID())

        // Create task 1
        val request1 = TaskCreateRequest(
            title = "Task 1",
            description = "Description 1",
            projectId = projectId,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task1 = taskService.create(request1, creatorId)

        // Create task 2
        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = projectId,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task2 = taskService.create(request2, creatorId)

        // Create task 3 (no project)
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task3 = taskService.create(request3, creatorId)

        // Find all tasks for the project
        val projectTasks = taskService.findAllByProjectId(projectId)

        // Verify the correct tasks were returned
        assertEquals(2, projectTasks.total)
        assertEquals(2, projectTasks.items.size)
        assertTrue(projectTasks.items.any { it.id == task1.id })
        assertTrue(projectTasks.items.any { it.id == task2.id })
        assertFalse(projectTasks.items.any { it.id == task3.id })

    }

    @Test
    fun `test find tasks by owner`() = runTest {
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
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task1 = taskService.create(request1, owner1Id)

        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task2 = taskService.create(request2, owner1Id)

        // Create task for owner2
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
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
    fun `test find tasks by assignee`() = runTest {
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
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task1 = taskService.create(request1, creatorId)

        val request2 = TaskCreateRequest(
            title = "Task 2",
            description = "Description 2",
            projectId = null,
            assigneeId = assignee1Id,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task2 = taskService.create(request2, creatorId)

        // Create task for assignee2
        val request3 = TaskCreateRequest(
            title = "Task 3",
            description = "Description 3",
            projectId = null,
            assigneeId = assignee2Id,
            priority = Priority.MEDIUM,
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
    fun `test find task by id`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator_find@example.com", displayName = "Task Creator Find")
        val assigneeId = createTestUser(email = "assignee_find@example.com", displayName = "Task Assignee Find")

        // Create a task
        val request = TaskCreateRequest(
            title = "Test Task",
            description = "Test Description",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
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
    fun `test find task by id - not found`() = runTest {
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
    fun `test create task`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator@example.com", displayName = "Task Creator")
        val assigneeId = createTestUser(email = "assignee@example.com", displayName = "Task Assignee")

        // Create an actual project in the database
        val projectResponse = projectService.createProject(
            creatorId.toUUID(), ProjectCreateRequest(
                name = "Test Project",
                description = "Test Project Description"
            )
        )
        val projectId = projectResponse.id

        // Assign the assignee to the project
        projectService.assignUserToProject(projectId.toUUID(), assigneeId.toUUID(), creatorId.toUUID())

        val dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val request = TaskCreateRequest(
            title = "New Task",
            description = "New Task Description",
            projectId = projectId,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
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
    fun `test create task with assignee not in project fails`() = runTest {
        // Create actual users in the database
        val creatorId =
            createTestUser(email = "creator_not_in_project@example.com", displayName = "Task Creator Not In Project")
        val assigneeId =
            createTestUser(email = "assignee_not_in_project@example.com", displayName = "Task Assignee Not In Project")

        // Create an actual project in the database
        val projectResponse = projectService.createProject(
            creatorId.toUUID(), ProjectCreateRequest(
                name = "Test Project Not In",
                description = "Test Project Description Not In"
            )
        )
        val projectId = projectResponse.id

        // Note: We deliberately do NOT assign the assignee to the project

        val dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val request = TaskCreateRequest(
            title = "New Task Not In Project",
            description = "New Task Description Not In Project",
            projectId = projectId,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = dueDate
        )

        assertFailsWith<ForbiddenException>(
            message = "You don't have permission to access Project with ID '518c257b-8a55-4f9a-ba37-8e18b9143e73'"
        ) {
            taskService.create(request, assigneeId)
        }
    }

    @Test
    fun `test create task with no assignee automatically assigns creator`() = runTest {
        // Create actual user in the database
        val creatorId =
            createTestUser(email = "creator_auto_assign@example.com", displayName = "Task Creator Auto Assign")

        // Create a task without specifying an assignee
        val request = TaskCreateRequest(
            title = "Auto Assigned Task",
            description = "This task should be auto-assigned to creator",
            projectId = null,
            assigneeId = null, // No assignee specified
            priority = Priority.MEDIUM,
            dueDate = null
        )

        // Create the task
        val task = taskService.create(request, creatorId)

        // Verify the task was created correctly and assigned to the creator
        assertNotNull(task)
        assertEquals(request.title, task.title)
        assertEquals(request.description, task.description)
        assertEquals(creatorId, task.assigneeId) // Creator should be the assignee
        assertEquals(creatorId, task.creatorId)
        assertEquals(TaskStatus.TODO, task.status)
    }

    @Test
    fun `test update task`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator2@example.com", displayName = "Task Creator 2")
        val assigneeId = createTestUser(email = "assignee2@example.com", displayName = "Task Assignee 2")

        // Create a task
        val createRequest = TaskCreateRequest(
            title = "Original Title",
            description = "Original Description",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(createRequest, creatorId)

        // Update the task
        val updateRequest = TaskUpdateRequest(
            title = "Updated Title",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS,
            priority = Priority.HIGH,
            dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        val updatedTask = taskService.update(task.id, updateRequest)

        // Verify the task was updated correctly
        assertNotNull(updatedTask)
        assertEquals(updateRequest.title, updatedTask.title)
        assertEquals(updateRequest.description, updatedTask.description)
        assertEquals(updateRequest.status, updatedTask.status)
        assertEquals(task.assigneeId, updatedTask.assigneeId) // Assignee should not change
        assertEquals(task.creatorId, updatedTask.creatorId) // Creator should not change
    }

    @Test
    fun `test update task - not found`() = runTest {
        // Try to update a task that doesn't exist
        val updateRequest = TaskUpdateRequest(
            title = "Updated Title",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS,
            priority = Priority.HIGH,
            dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )

        try {
            taskService.update(UUID.randomUUID().toString(), updateRequest)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }

    @Test
    fun `test delete task`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator_delete@example.com", displayName = "Task Creator Delete")
        val assigneeId = createTestUser(email = "assignee_delete@example.com", displayName = "Task Assignee Delete")

        // Create a task
        val request = TaskCreateRequest(
            title = "Task to Delete",
            description = "This task will be deleted",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Delete the task
        val result = taskService.delete(task.id)

        // Verify the task was deleted
        assertTrue(result)

        // Try to find the deleted task
        try {
            taskService.findById(task.id)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }

    @Test
    fun `test delete task - not found`() = runTest {
        // Try to delete a task that doesn't exist
        val result = taskService.delete(UUID.randomUUID().toString())

        // Verify the result is false
        assertFalse(result)
    }

    @Test
    fun `test assign task`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator_assign@example.com", displayName = "Task Creator Assign")
        val assignee1Id = createTestUser(email = "assignee1_assign@example.com", displayName = "Task Assignee 1 Assign")
        val assignee2Id = createTestUser(email = "assignee2_assign@example.com", displayName = "Task Assignee 2 Assign")

        // Create a task
        val request = TaskCreateRequest(
            title = "Task to Assign",
            description = "This task will be assigned",
            projectId = null,
            assigneeId = assignee1Id,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Assign the task to a different assignee
        val updatedTask = taskService.assign(task.id, assignee2Id)

        // Verify the task was assigned correctly
        assertNotNull(updatedTask)
        assertEquals(task.id, updatedTask.id)
        assertEquals(task.title, updatedTask.title)
        assertEquals(task.description, updatedTask.description)
        assertEquals(assignee2Id, updatedTask.assigneeId) // Assignee should be updated
        assertEquals(task.creatorId, updatedTask.creatorId) // Creator should not change
    }

    @Test
    fun `test assign task - not found`() = runTest {
        // Create actual user in the database
        val assigneeId =
            createTestUser(email = "assignee_not_found@example.com", displayName = "Task Assignee Not Found")

        // Try to assign a task that doesn't exist
        try {
            taskService.assign(UUID.randomUUID().toString(), assigneeId)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }

    @Test
    fun `test change task status`() = runTest {
        // Create actual users in the database
        val creatorId = createTestUser(email = "creator_status@example.com", displayName = "Task Creator Status")
        val assigneeId = createTestUser(email = "assignee_status@example.com", displayName = "Task Assignee Status")

        // Create a task
        val request = TaskCreateRequest(
            title = "Task to Change Status",
            description = "This task's status will be changed",
            projectId = null,
            assigneeId = assigneeId,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(request, creatorId)

        // Change the task status
        val updatedTask = taskService.changeStatus(task.id, TaskStatus.IN_PROGRESS.name)

        // Verify the task status was changed correctly
        assertNotNull(updatedTask)
        assertEquals(task.id, updatedTask.id)
        assertEquals(task.title, updatedTask.title)
        assertEquals(task.description, updatedTask.description)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status) // Status should be updated
        assertEquals(task.assigneeId, updatedTask.assigneeId) // Assignee should not change
        assertEquals(task.creatorId, updatedTask.creatorId) // Creator should not change
    }

    @Test
    fun `test change task status - not found`() = runTest {
        // Try to change the status of a task that doesn't exist
        try {
            taskService.changeStatus(UUID.randomUUID().toString(), TaskStatus.IN_PROGRESS.name)
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message.contains("Task"))
        }
    }
}
