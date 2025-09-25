package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import com.danioliveira.taskmanager.routes.toUUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskRepositoryImplTest : KoinTest {

    private val taskRepository: TaskRepository by inject()
    private val userRepository: UserRepository by inject()
    private val projectRepository: ProjectRepository by inject()

    private lateinit var testUserId: UUID
    private lateinit var testProjectId: UUID

    @Before
    fun setUp() = runTest {
        TestDatabase.init()
        startKoin {
            modules(
                module {
                    single<TaskRepository> { TaskRepositoryImpl() }
                    single<UserRepository> { UserRepositoryImpl() }
                    single<ProjectRepository> { ProjectRepositoryImpl() }
                }
            )
        }

        // Create a test user and project

        val user = dbQuery {
            with(userRepository) {
                create("test@example.com", "password", "Test User", null)
            }
        }
        testUserId = UUID.fromString(user.id)

        val project = dbQuery {
            with(projectRepository) {
                create("Test Project", "Test Description", testUserId)
            }
        }
        testProjectId = UUID.fromString(project.id)
    }

    @After
    fun tearDown() = runTest {
        TestDatabase.clearDatabase()
        stopKoin()
    }

    @Test
    fun `test create and find task by id`() = runTest {
        val title = "Test Task"
        val description = "Test Description"
        val status = TaskStatus.TODO
        val priority = Priority.MEDIUM
        val dueDate = LocalDateTime.parse("2024-12-31T23:59:59")

        // Create a task
        val task = dbQuery {
            taskRepository.create(
                title,
                description,
                testProjectId,
                testUserId,
                testUserId,
                status,
                priority,
                dueDate
            )
        }

        // Verify the task was created correctly
        assertNotNull(task)
        assertEquals(title, task.title)
        assertEquals(description, task.description)
        assertEquals(status, task.status)
        assertEquals(testProjectId.toString(), task.projectId)
        assertEquals(testUserId.toString(), task.assigneeId)
        assertEquals(testUserId.toString(), task.creatorId)

        // Find the task by ID
        val foundTask = dbQuery { taskRepository.findById(task.id) }

        // Verify the task was found
        assertNotNull(foundTask)
        assertEquals(task.id, foundTask.id)
        assertEquals(title, foundTask.title)
        assertEquals(description, foundTask.description)
        assertEquals(status, foundTask.status)
    }

    @Test
    fun `test find tasks by project id`() = runTest {
        // Create tasks for the test project
        val task1 = dbQuery {
            taskRepository.create(
                "Task 1",
                "Description 1",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.MEDIUM,
                null
            )
        }

        val task2 = dbQuery {
            taskRepository.create(
                "Task 2",
                "Description 2",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.HIGH,
                null
            )
        }

        // Create a task without a project
        val task3 = dbQuery {
            taskRepository.create(
                "Task 3",
                "Description 3",
                null,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.LOW,
                null
            )
        }

        // Find tasks by project ID
        val projectTasks = dbQuery {
            taskRepository.findAllByProjectId(testProjectId, 0, 10)
        }

        // Verify the correct tasks were found
        assertEquals(2, projectTasks.total)
        assertEquals(2, projectTasks.items.size)
        assertTrue(projectTasks.items.any { it.id == task1.id })
        assertTrue(projectTasks.items.any { it.id == task2.id })
        assertFalse(projectTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find tasks by owner id`() = runTest {
        // Create a second user
        val secondUser = dbQuery {
            userRepository.create("second@example.com", "password", "Second User", null)
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different creators
        val task1 = dbQuery {
            taskRepository.create(
                "Task 1",
                "Description 1",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.MEDIUM,
                null
            )
        }

        val task2 = dbQuery {
            taskRepository.create(
                "Task 2",
                "Description 2",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.HIGH,
                null
            )
        }

        val task3 = dbQuery {
            taskRepository.create(
                "Task 3",
                "Description 3",
                testProjectId,
                testUserId,
                secondUserId,
                TaskStatus.TODO,
                Priority.LOW,
                null
            )
        }

        // Find tasks by owner ID (first user)
        val ownerTasks = dbQuery {
            taskRepository.findAllByOwnerId(testUserId, 0, 10)
        }

        // Verify the correct tasks were found
        assertEquals(2, ownerTasks.total)
        assertEquals(2, ownerTasks.items.size)
        assertTrue(ownerTasks.items.any { it.id == task1.id })
        assertTrue(ownerTasks.items.any { it.id == task2.id })
        assertFalse(ownerTasks.items.any { it.id == task3.id })

        // Find tasks by owner ID (second user)
        val secondOwnerTasks = dbQuery {
            taskRepository.findAllByOwnerId(secondUserId, 0, 10)
        }

        // Verify the correct tasks were found
        assertEquals(1, secondOwnerTasks.total)
        assertEquals(1, secondOwnerTasks.items.size)
        assertTrue(secondOwnerTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find tasks by assignee id`() = runTest {
        // Create a second user
        val secondUser = dbQuery {
            userRepository.create("second@example.com", "password", "Second User", null)
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different assignees
        val task1 = dbQuery {
            taskRepository.create(
                "Task 1",
                "Description 1",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.MEDIUM,
                null
            )
        }

        val task2 = dbQuery {
            taskRepository.create(
                "Task 2",
                "Description 2",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.HIGH,
                null
            )
        }

        val task3 = dbQuery {
            taskRepository.create(
                "Task 3",
                "Description 3",
                testProjectId,
                secondUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.LOW,
                null
            )
        }

        // Find tasks by assignee ID (first user)
        val assigneeTasks = dbQuery {
            taskRepository.findAllByAssigneeId(testUserId, 0, 10)
        }

        // Verify the correct tasks were found
        assertEquals(2, assigneeTasks.total)
        assertEquals(2, assigneeTasks.items.size)
        assertTrue(assigneeTasks.items.any { it.id == task1.id })
        assertTrue(assigneeTasks.items.any { it.id == task2.id })
        assertFalse(assigneeTasks.items.any { it.id == task3.id })

        // Find tasks by assignee ID (second user)
        val secondAssigneeTasks = dbQuery {
            taskRepository.findAllByAssigneeId(secondUserId, 0, 10)
        }

        // Verify the correct tasks were found
        assertEquals(1, secondAssigneeTasks.total)
        assertEquals(1, secondAssigneeTasks.items.size)
        assertTrue(secondAssigneeTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test update task`() = runBlocking {
        // Create a task
        val task = dbQuery {
            taskRepository.create(
                "Original Task",
                "Original Description",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.LOW,
                null
            )
        }

        // Update the task
        val updatedTask = dbQuery {
            taskRepository.update(
                task.id,
                "Updated Task",
                "Updated Description",
                TaskStatus.IN_PROGRESS,
                Priority.HIGH,
                LocalDateTime.parse("2024-12-31T23:59:59"),
                testUserId
            )
        }

        // Verify the task was updated
        assertNotNull(updatedTask)
        assertEquals("Updated Task", updatedTask.title)
        assertEquals("Updated Description", updatedTask.description)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status)
        assertEquals(Priority.HIGH, updatedTask.priority)
    }

    @Test
    fun `test delete task`() = runBlocking {
        // Create a task
        val task = dbQuery {
            taskRepository.create(
                "Task to Delete",
                "This task will be deleted",
                testProjectId,
                testUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.MEDIUM,
                null
            )
        }

        // Delete the task
        val deleted = dbQuery {
            taskRepository.delete(task.id.toUUID())
        }

        // Verify the task was deleted
        assertTrue(deleted)

        // Try to find the deleted task
        val foundTask = dbQuery {
            taskRepository.findById(task.id)
        }

        // Verify the task was not found
        assertEquals(null, foundTask)
    }

    @Test
    fun `test delete non-existent task`() = runBlocking {
        // Try to delete a task that doesn't exist
        val deleted = dbQuery {
            taskRepository.delete(UUID.randomUUID())
        }

        // Verify the deletion failed
        assertFalse(deleted)
    }

    @Test
    fun `test find tasks by assignee id with query filter`() = runTest {
        // Create a second user
        val secondUser = dbQuery {
            userRepository.create("second@example.com", "password", "Second User", null)
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different titles
        val task1 = dbQuery {
            taskRepository.create(
                "Important Task",
                "Description 1",
                testProjectId,
                secondUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.HIGH,
                null
            )
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create(
                    "Regular Task",
                    "Description 2",
                    testProjectId,
                    secondUserId,
                    testUserId,
                    TaskStatus.TODO,
                    Priority.MEDIUM,
                    null
                )
            }
        }

        val task3 = dbQuery {
            taskRepository.create(
                "Another Important Task",
                "Description 3",
                testProjectId,
                secondUserId,
                testUserId,
                TaskStatus.TODO,
                Priority.LOW,
                null
            )
        }

        // Find tasks by assignee ID with query filter "Important"
        val filteredTasks = dbQuery {
            taskRepository.findAllByAssigneeId(secondUserId, 0, 10, "Important")
        }

        // Verify the correct tasks were found
        assertEquals(2, filteredTasks.total)
        assertEquals(2, filteredTasks.items.size)
        assertTrue(filteredTasks.items.any { it.id == task1.id })
        assertTrue(filteredTasks.items.any { it.id == task3.id })
        assertFalse(filteredTasks.items.any { it.id == task2.id })
    }

    @Test
    fun `test get user task progress`() = runTest {
        // Create a second user
        val secondUser = dbQuery {
            userRepository.create(
                email = "second@example.com",
                passwordHash = "password",
                displayName = "Second User",
                googleId = null
            )
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different statuses
        // 2 TODO tasks
        repeat(2) {
            dbQuery {
                taskRepository.create(
                    "TODO Task $it",
                    "Description for TODO task $it",
                    testProjectId,
                    secondUserId,
                    testUserId,
                    TaskStatus.TODO,
                    Priority.MEDIUM,
                    null
                )
            }
        }

        // 3 DONE tasks
        repeat(3) {
            dbQuery {
                taskRepository.create(
                    "Done Task $it",
                    "Description for done task $it",
                    testProjectId,
                    secondUserId,
                    testUserId,
                    TaskStatus.DONE,
                    Priority.MEDIUM,
                    null
                )
            }
        }

        // Get task progress for the second user
        val progress = dbQuery {
            taskRepository.getUserTaskProgress(secondUserId)
        }

        // Verify the task progress
        assertEquals(5, progress.totalTasks)
        assertEquals(3, progress.completedTasks)
    }
}
