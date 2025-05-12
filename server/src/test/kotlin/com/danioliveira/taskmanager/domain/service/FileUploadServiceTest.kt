package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.response.FileResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.getTestModule
import com.danioliveira.taskmanager.utils.IS3Client
import com.danioliveira.taskmanager.utils.S3ClientFactory
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Transaction
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class FileUploadServiceTest : KoinTest {
    private val taskService: TaskService by inject()

    // Mock S3Client that will be used for testing
    private class MockS3Client : IS3Client {
        override suspend fun uploadFile(fileName: String, contentType: String, fileBytes: ByteArray): String {
            // Just return a mock URL without actually uploading anything
            return "http://mock-endpoint/mock-bucket/$fileName"
        }
    }

    // Mock repository that will be used for testing
    private class MockTaskRepository : TaskRepository {
        private val files = mutableMapOf<String, MutableList<FileResponse>>()

        override suspend fun Transaction.findAllByProjectId(projectId: String?, page: Int, size: Int) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.findAllByOwnerId(ownerId: String, page: Int, size: Int) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.findAllByAssigneeId(assigneeId: String, page: Int, size: Int, query: String?) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.findById(id: String) =
            if (id == "non-existent-id") null else TaskResponse(
                id = id,
                title = "Test Task",
                description = "Test Description",
                status = TaskStatus.TODO,
                priority = Priority.MEDIUM,
                dueDate = "",
                projectId = null,
                assigneeId = "test-user",
                creatorId = "test-user"
            )

        override suspend fun Transaction.create(
            title: String,
            description: String?,
            projectId: UUID?,
            assigneeId: UUID?,
            creatorId: UUID,
            status: TaskStatus,
            dueDate: java.time.LocalDateTime?
        ) =
            TaskResponse(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description ?: "",
                status = status,
                priority = Priority.MEDIUM,
                dueDate = dueDate?.toString() ?: "",
                projectId = projectId?.toString(),
                assigneeId = assigneeId?.toString(),
                creatorId = creatorId.toString()
            )

        override suspend fun Transaction.update(
            id: String,
            title: String,
            description: String?,
            status: TaskStatus,
            priority: Priority,
            dueDate: String?,
            assigneeId: String?
        ) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.delete(id: String) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.findAllTasksForUser(userId: String) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.getUserTaskProgress(userId: String) =
            throw NotImplementedError("Not needed for file upload tests")

        override suspend fun Transaction.getTaskFiles(taskId: String): List<FileResponse> =
            files[taskId] ?: emptyList()

        override suspend fun Transaction.uploadTaskFile(
            taskId: String,
            fileName: String,
            contentType: String,
            uploaderId: String,
            s3Url: String
        ): FileResponse {
            val fileResponse = FileResponse(
                id = UUID.randomUUID().toString(),
                name = fileName,
                size = "1 MB",
                uploadedDate = java.time.LocalDateTime.now().toString(),
                taskId = taskId,
                url = s3Url,
                contentType = contentType
            )

            if (!files.containsKey(taskId)) {
                files[taskId] = mutableListOf()
            }
            files[taskId]?.add(fileResponse)

            return fileResponse
        }
    }

    @Before
    fun setUp() {
        // Initialize the H2 database
        TestDatabase.init()

        // Override the S3ClientFactory.createFromEnv method to return our mock S3Client
        val mockS3Client = MockS3Client()
        S3ClientFactory.overrideForTesting(mockS3Client)

        // Create a test module with our mock repository
        val testModule = module {
            single<TaskRepository> { MockTaskRepository() }
        }

        // Start Koin with the test module
        startKoin {
            modules(getTestModule(ApplicationConfig("application_test.conf")), testModule)
        }
    }

    @After
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()

        // Reset the S3ClientFactory
        S3ClientFactory.resetOverride()

        // Stop Koin
        stopKoin()
    }

    @Test
    fun `test upload valid file to task`() = runBlocking {
        // Create a user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Test User"
        )

        // Create a task
        val taskRequest = TaskCreateRequest(
            title = "Test Task",
            description = "Test Description",
            projectId = null,
            assigneeId = null,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(taskRequest, userId)

        // Create a test PDF file
        val fileName = "test.pdf"
        val contentType = "application/pdf"
        val fileContent = "This is a test PDF file".toByteArray()

        // Upload the file
        val fileResponse = taskService.uploadTaskFile(
            taskId = task.id,
            fileName = fileName,
            contentType = contentType,
            fileBytes = fileContent,
            uploaderId = userId
        )

        // Verify the file was uploaded successfully
        assertNotNull(fileResponse)
        assertEquals(fileName, fileResponse.name)
        assertEquals(contentType, fileResponse.contentType)
        assertEquals(task.id, fileResponse.taskId)

        // Verify the file can be retrieved
        val files = taskService.getTaskFiles(task.id)
        assertEquals(1, files.size)
        assertEquals(fileName, files[0].name)
    }

    @Test
    fun `test upload invalid file type to task`() = runBlocking {
        // Create a user
        val userId = createTestUser(
            email = "user2@example.com",
            displayName = "Test User 2"
        )

        // Create a task
        val taskRequest = TaskCreateRequest(
            title = "Test Task 2",
            description = "Test Description 2",
            projectId = null,
            assigneeId = null,
            priority = Priority.MEDIUM,
            dueDate = null
        )
        val task = taskService.create(taskRequest, userId)

        // Create a test file with invalid type
        val fileName = "test.exe"
        val contentType = "application/octet-stream"
        val fileContent = "This is a test executable file".toByteArray()

        // Try to upload the file, which should fail
        try {
            taskService.uploadTaskFile(
                taskId = task.id,
                fileName = fileName,
                contentType = contentType,
                fileBytes = fileContent,
                uploaderId = userId
            )
            fail("Expected ValidationException was not thrown")
        } catch (e: ValidationException) {
            // Expected exception
            assertTrue(e.message.contains("File type not allowed") == true)
        }

        // Verify no files were uploaded
        val files = taskService.getTaskFiles(task.id)
        assertEquals(0, files.size)
    }

    @Test
    fun `test upload file to non-existent task`() = runBlocking {
        // Create a user
        val userId = createTestUser(
            email = "user3@example.com",
            displayName = "Test User 3"
        )

        // Create a test PDF file
        val fileName = "test.pdf"
        val contentType = "application/pdf"
        val fileContent = "This is a test PDF file".toByteArray()

        // Try to upload the file to a non-existent task, which should fail
        try {
            taskService.uploadTaskFile(
                taskId = "non-existent-id",
                fileName = fileName,
                contentType = contentType,
                fileBytes = fileContent,
                uploaderId = userId
            )
            fail("Expected NotFoundException was not thrown")
        } catch (e: NotFoundException) {
            // Expected exception
            assertTrue(e.message?.contains("Task") == true)
        }
    }
}
