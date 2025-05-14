package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaskRepositoryImplTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var testUserId: UUID
    private lateinit var testProjectId: UUID

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()
        taskRepository = TaskRepositoryImpl()
        userRepository = UserRepositoryImpl()
        projectRepository = ProjectRepositoryImpl()

        // Create a test user to be used as the creator/assignee in task tests
        val user = dbQuery {
            with(userRepository) {
                create("test@example.com", "password", "Test User", null)
            }
        }
        testUserId = UUID.fromString(user.id)

        // Create a test project to be used in task tests
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
    fun `test create and find task by id`() = runBlocking {
        // Create a task
        val title = "Test Task"
        val description = "This is a test task"
        val status = TaskStatus.TODO
        val dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val task = dbQuery {
            with(taskRepository) {
                create(title, description, testProjectId, testUserId, testUserId, status, dueDate)
            }
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
        val foundTask = dbQuery {
            with(taskRepository) {
                findById(task.id)
            }
        }

        // Verify the task was found
        assertNotNull(foundTask)
        assertEquals(task.id, foundTask.id)
        assertEquals(title, foundTask.title)
        assertEquals(description, foundTask.description)
        assertEquals(status, foundTask.status)
    }

    @Test
    fun `test find tasks by project id`() = runBlocking {
        // Create tasks for the test project
        val task1 = dbQuery {
            with(taskRepository) {
                create("Task 1", "Description 1", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create("Task 2", "Description 2", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        // Create a task without a project
        val task3 = dbQuery {
            with(taskRepository) {
                create("Task 3", "Description 3", null, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        // Find tasks by project ID
        val projectTasks = dbQuery {
            with(taskRepository) {
                findAllByProjectId(testProjectId.toString(), 0, 10)
            }
        }

        // Verify the correct tasks were found
        assertEquals(2, projectTasks.total)
        assertEquals(2, projectTasks.items.size)
        assertTrue(projectTasks.items.any { it.id == task1.id })
        assertTrue(projectTasks.items.any { it.id == task2.id })
        assertFalse(projectTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find tasks by owner id`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different creators
        val task1 = dbQuery {
            with(taskRepository) {
                create("Task 1", "Description 1", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create("Task 2", "Description 2", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task3 = dbQuery {
            with(taskRepository) {
                create("Task 3", "Description 3", testProjectId, testUserId, secondUserId, TaskStatus.TODO, null)
            }
        }

        // Find tasks by owner ID (first user)
        val ownerTasks = dbQuery {
            with(taskRepository) {
                findAllByOwnerId(testUserId.toString(), 0, 10)
            }
        }

        // Verify the correct tasks were found
        assertEquals(2, ownerTasks.total)
        assertEquals(2, ownerTasks.items.size)
        assertTrue(ownerTasks.items.any { it.id == task1.id })
        assertTrue(ownerTasks.items.any { it.id == task2.id })
        assertFalse(ownerTasks.items.any { it.id == task3.id })

        // Find tasks by owner ID (second user)
        val secondOwnerTasks = dbQuery {
            with(taskRepository) {
                findAllByOwnerId(secondUserId.toString(), 0, 10)
            }
        }

        // Verify the correct tasks were found
        assertEquals(1, secondOwnerTasks.total)
        assertEquals(1, secondOwnerTasks.items.size)
        assertTrue(secondOwnerTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find tasks by assignee id`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different assignees
        val task1 = dbQuery {
            with(taskRepository) {
                create("Task 1", "Description 1", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create("Task 2", "Description 2", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task3 = dbQuery {
            with(taskRepository) {
                create("Task 3", "Description 3", testProjectId, secondUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        // Find tasks by assignee ID (first user)
        val assigneeTasks = dbQuery {
            with(taskRepository) {
                findAllByAssigneeId(testUserId.toString(), 0, 10)
            }
        }

        // Verify the correct tasks were found
        assertEquals(2, assigneeTasks.total)
        assertEquals(2, assigneeTasks.items.size)
        assertTrue(assigneeTasks.items.any { it.id == task1.id })
        assertTrue(assigneeTasks.items.any { it.id == task2.id })
        assertFalse(assigneeTasks.items.any { it.id == task3.id })

        // Find tasks by assignee ID (second user)
        val secondAssigneeTasks = dbQuery {
            with(taskRepository) {
                findAllByAssigneeId(secondUserId.toString(), 0, 10)
            }
        }

        // Verify the correct tasks were found
        assertEquals(1, secondAssigneeTasks.total)
        assertEquals(1, secondAssigneeTasks.items.size)
        assertTrue(secondAssigneeTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test find all tasks for user`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different creators and assignees
        val task1 = dbQuery {
            with(taskRepository) {
                create("Task 1", "Description 1", testProjectId, testUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create("Task 2", "Description 2", testProjectId, secondUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task3 = dbQuery {
            with(taskRepository) {
                create("Task 3", "Description 3", testProjectId, testUserId, secondUserId, TaskStatus.TODO, null)
            }
        }

        // Find all tasks for the first user (both as creator and assignee)
        val userTasks = dbQuery {
            with(taskRepository) {
                findAllTasksForUser(testUserId.toString())
            }
        }

        // Verify the correct tasks were found
        assertEquals(3, userTasks.total)
        assertEquals(3, userTasks.items.size)
        assertTrue(userTasks.items.any { it.id == task1.id })
        assertTrue(userTasks.items.any { it.id == task2.id })
        assertTrue(userTasks.items.any { it.id == task3.id })

        // Find all tasks for the second user (both as creator and assignee)
        val secondUserTasks = dbQuery {
            with(taskRepository) {
                findAllTasksForUser(secondUserId.toString())
            }
        }

        // Verify the correct tasks were found
        assertEquals(2, secondUserTasks.total)
        assertEquals(2, secondUserTasks.items.size)
        assertTrue(secondUserTasks.items.any { it.id == task2.id })
        assertTrue(secondUserTasks.items.any { it.id == task3.id })
    }

    @Test
    fun `test update task`() = runBlocking {
        // Create a task
        val task = dbQuery {
            with(taskRepository) {
                create(
                    "Original Title",
                    "Original Description",
                    testProjectId,
                    testUserId,
                    testUserId,
                    TaskStatus.TODO,
                    null
                )
            }
        }

        // Create a second user for reassignment
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Update the task
        val updatedTask = dbQuery {
            with(taskRepository) {
                update(
                    task.id,
                    "Updated Title",
                    "Updated Description",
                    TaskStatus.IN_PROGRESS,
                    Priority.HIGH,
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    secondUserId.toString()
                )
            }
        }

        // Verify the update was successful
        assertNotNull(updatedTask)
        assertEquals("Updated Title", updatedTask.title)
        assertEquals("Updated Description", updatedTask.description)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.status)
        assertEquals(secondUserId.toString(), updatedTask.assigneeId)

        // Find the task to verify the changes
        val foundTask = dbQuery {
            with(taskRepository) {
                findById(task.id)
            }
        }

        // Verify the task was updated correctly
        assertNotNull(foundTask)
        assertEquals("Updated Title", foundTask.title)
        assertEquals("Updated Description", foundTask.description)
        assertEquals(TaskStatus.IN_PROGRESS, foundTask.status)
        assertEquals(secondUserId.toString(), foundTask.assigneeId)
    }

    @Test
    fun `test delete task`() = runBlocking {
        // Create a task
        val task = dbQuery {
            with(taskRepository) {
                create(
                    "Task to Delete",
                    "This task will be deleted",
                    testProjectId,
                    testUserId,
                    testUserId,
                    TaskStatus.TODO,
                    null
                )
            }
        }

        // Verify the task exists
        val foundTask = dbQuery {
            with(taskRepository) {
                findById(task.id)
            }
        }
        assertNotNull(foundTask)

        // Delete the task
        val deleted = dbQuery {
            with(taskRepository) {
                delete(task.id)
            }
        }

        // Verify the deletion was successful
        assertTrue(deleted)

        // Verify the task no longer exists
        val deletedTask = dbQuery {
            with(taskRepository) {
                findById(task.id)
            }
        }
        assertNull(deletedTask)
    }

    @Test
    fun `test update non-existent task`() = runBlocking {
        // Try to update a task that doesn't exist
        val updatedTask = dbQuery {
            with(taskRepository) {
                update(
                    UUID.randomUUID().toString(),
                    "Updated Title",
                    "Updated Description",
                    TaskStatus.IN_PROGRESS,
                    Priority.HIGH,
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    testUserId.toString()
                )
            }
        }

        // Verify the update failed
        assertNull(updatedTask)
    }

    @Test
    fun `test delete non-existent task`() = runBlocking {
        // Try to delete a task that doesn't exist
        val deleted = dbQuery {
            with(taskRepository) {
                delete(UUID.randomUUID().toString())
            }
        }

        // Verify the deletion failed
        assertFalse(deleted)
    }

    @Test
    fun `test find tasks by assignee id with query filter`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different titles
        val task1 = dbQuery {
            with(taskRepository) {
                create(
                    "Important Task",
                    "Description 1",
                    testProjectId,
                    secondUserId,
                    testUserId,
                    TaskStatus.TODO,
                    null
                )
            }
        }

        val task2 = dbQuery {
            with(taskRepository) {
                create("Regular Task", "Description 2", testProjectId, secondUserId, testUserId, TaskStatus.TODO, null)
            }
        }

        val task3 = dbQuery {
            with(taskRepository) {
                create(
                    "Another Important Task",
                    "Description 3",
                    testProjectId,
                    secondUserId,
                    testUserId,
                    TaskStatus.TODO,
                    null
                )
            }
        }

        // Find tasks by assignee ID with query filter "Important"
        val filteredTasks = dbQuery {
            with(taskRepository) {
                findAllByAssigneeId(secondUserId.toString(), 0, 10, "Important")
            }
        }

        // Verify the correct tasks were found
        assertEquals(2, filteredTasks.total)
        assertEquals(2, filteredTasks.items.size)
        assertTrue(filteredTasks.items.any { it.id == task1.id })
        assertTrue(filteredTasks.items.any { it.id == task3.id })
        assertFalse(filteredTasks.items.any { it.id == task2.id })
    }

    @Test
    fun `test get user task progress`() = runBlocking {
        // Create a second user
        val secondUser = dbQuery {
            with(userRepository) {
                create("second@example.com", "password", "Second User", null)
            }
        }
        val secondUserId = UUID.fromString(secondUser.id)

        // Create tasks with different statuses
        // 2 TODO tasks
        repeat(2) {
            dbQuery {
                with(taskRepository) {
                    create(
                        "TODO Task $it",
                        "Description for TODO task $it",
                        testProjectId,
                        secondUserId,
                        testUserId,
                        TaskStatus.TODO,
                        null
                    )
                }
            }
        }

        // 3 DONE tasks
        repeat(3) {
            dbQuery {
                with(taskRepository) {
                    create(
                        "Done Task $it",
                        "Description for done task $it",
                        testProjectId,
                        secondUserId,
                        testUserId,
                        TaskStatus.DONE,
                        null
                    )
                }
            }
        }

        // Get task progress for the second user
        val progress = dbQuery {
            with(taskRepository) {
                getUserTaskProgress(secondUserId.toString())
            }
        }

        // Verify the task progress
        assertEquals(5, progress.totalTasks)
        assertEquals(3, progress.completedTasks)
    }

    @Test
    fun `test upload and get task files`() = runBlocking {
        // Create a task
        val task = dbQuery {
            with(taskRepository) {
                create(
                    "Task with Files",
                    "This task will have files",
                    testProjectId,
                    testUserId,
                    testUserId,
                    TaskStatus.TODO,
                    null
                )
            }
        }

        // Upload files for the task
        val file1 = dbQuery {
            with(taskRepository) {
                uploadTaskFile(
                    task.id,
                    "document.pdf",
                    "application/pdf",
                    testUserId.toString(),
                    "https://example.com/files/document.pdf"
                )
            }
        }

        val file2 = dbQuery {
            with(taskRepository) {
                uploadTaskFile(
                    task.id,
                    "image.jpg",
                    "image/jpeg",
                    testUserId.toString(),
                    "https://example.com/files/image.jpg"
                )
            }
        }

        // Get files for the task
        val files = dbQuery {
            with(taskRepository) {
                getTaskFiles(task.id)
            }
        }

        // Verify the files were uploaded and retrieved correctly
        assertEquals(2, files.size)
        assertTrue(files.any { it.id == file1.id })
        assertTrue(files.any { it.id == file2.id })

        // Verify file details
        val retrievedFile1 = files.find { it.id == file1.id }
        assertNotNull(retrievedFile1)
        assertEquals("document.pdf", retrievedFile1.name)
        assertEquals("https://example.com/files/document.pdf", retrievedFile1.url)
        assertEquals(task.id, retrievedFile1.taskId)

        val retrievedFile2 = files.find { it.id == file2.id }
        assertNotNull(retrievedFile2)
        assertEquals("image.jpg", retrievedFile2.name)
        assertEquals("https://example.com/files/image.jpg", retrievedFile2.url)
        assertEquals(task.id, retrievedFile2.taskId)
    }
}
