package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.TaskAssignRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskStatusChangeRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.response.PaginatedResponse
import com.danioliveira.taskmanager.api.response.TaskResponse
import com.danioliveira.taskmanager.api.routes.Projects
import com.danioliveira.taskmanager.api.routes.Tasks
import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.generateTestToken
import com.danioliveira.taskmanager.withAuth
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.property
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.danioliveira.taskmanager.domain.JwtConfig as DomainJwtConfig

class TaskRoutesTest : KoinTest {

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()
    }

    @After
    fun tearDown() = runBlocking {
        // Clear the database after each test
        TestDatabase.clearDatabase()

        // Stop Koin
        stopKoin()
    }

    //
    @Test
    fun `test get tasks owned by user - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a test task
        val taskTitle = "Test Task"
        val taskDescription = "Test Description"

        // Create a task using the API
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Verify the task was created successfully
        assertEquals(HttpStatusCode.Created, createResponse.status)

        // Get tasks
        val response = client.get(Tasks.Owned()) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<PaginatedResponse<TaskResponse>>()
        val items = responseBody.items

        // Verify the response contains the created task
        assertNotNull(items)
        assertEquals(1, items.size)
        assertEquals(taskTitle, items[0].title)
        assertEquals(taskDescription, items[0].description)
    }

    //
    @Test
    fun `test get tasks owned by user - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Get tasks without authentication
        val response = client.get(Tasks.Owned())

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    //
    @Test
    fun `test get tasks by project id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a project
        val projectResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val projectResponseBody = projectResponse.body<com.danioliveira.taskmanager.api.response.ProjectResponse>()
        val projectId = projectResponseBody.id

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project (needed for the assignee validation)
        val projectResource = Projects.Id(projectId = projectId)
        client.post(Projects.Id.Assign(projectResource)) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(mapOf("userId" to userId))
        }

        // Create a task for the project
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Project Task",
                    description = "Task for the project",
                    projectId = projectId,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Create a task without a project
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Non-Project Task",
                    description = "Task without a project",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get tasks for the project
        val projectTasksResource = Projects.Id(projectId = projectId)
        val response = client.get(Projects.Id.Tasks(projectTasksResource)) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<PaginatedResponse<TaskResponse>>()
        val items = responseBody.items

        // Verify the response contains only the project task
        assertNotNull(items)
        assertEquals(1, items.size)
        assertEquals("Project Task", items[0].title)
    }

    //
    @Test
    fun `test get tasks by assignee id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        // Create test users
        val user1Id = createTestUser(
            email = "user1@example.com",
            displayName = "User 1"
        )

        val user2Id = createTestUser(
            email = "user2@example.com",
            displayName = "User 2"
        )

        // Create a task assigned to user1
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "User 1 Task",
                    description = "Task for User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Create a task assigned to user2 (auth as user2 since POST /v1/tasks forces assignee to current user)
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user2Id, "user2@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Assigned Task",
                    description = "Task assigned to user",
                    projectId = null,
                    assigneeId = user2Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get tasks assigned to current user (user1)
        val response = client.get(Tasks.Assigned()) {
            withAuth(generateTestToken(user1Id, "user1@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<PaginatedResponse<TaskResponse>>()
        val items = responseBody.items

        // Verify the response contains only tasks assigned to user1
        assertNotNull(items)
        assertEquals(1, items.size)
        assertEquals("User 1 Task", items[0].title)
    }

    //
    @Test
    fun `test get tasks by user`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create test users
        val user1Id = createTestUser(
            email = "user1@example.com",
            displayName = "User 1"
        )

        val user2Id = createTestUser(
            email = "user2@example.com",
            displayName = "User 2"
        )

        // Create tasks created by user1
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Task 1",
                    description = "Task created by User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Task 2",
                    description = "Another task created by User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Create a task created by user2
        client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user2Id, "user2@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Task 3",
                    description = "Task created by User 2",
                    projectId = null,
                    assigneeId = user2Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get tasks created by user1
        val response = client.get(Tasks.Owned()) {
            withAuth(generateTestToken(user1Id, "user1@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<PaginatedResponse<TaskResponse>>()
        val items = responseBody.items

        // Verify the response contains only the tasks created by user1
        assertNotNull(items)
        assertEquals(2, items.size)
        assertTrue(items.any { it.title == "Task 1" })
        assertTrue(items.any { it.title == "Task 2" })
        assertFalse(items.any { it.title == "Task 3" })
    }

    //
    @Test
    fun `test create task - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val taskTitle = "New Task"
        val taskDescription = "New Description"

        val response = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.Created, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the response contains the created task
        assertEquals(taskTitle, responseBody.title)
        assertEquals(taskDescription, responseBody.description)
        assertEquals(userId, responseBody.assigneeId)
        assertEquals(userId, responseBody.creatorId)
        assertEquals(TaskStatus.TODO, responseBody.status)
    }

    @Test
    fun `test create task - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Try to create a task without authentication
        val response = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            setBody(
                TaskCreateRequest(
                    title = "New Task",
                    description = "New Description",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    //
    @Test
    fun `test create task for current user`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task for the current user
        val taskTitle = "User Task"
        val taskDescription = "Task for the current user"

        val response = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = null,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.Created, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the response contains the created task with the current user as assignee
        assertEquals(taskTitle, responseBody.title)
        assertEquals(taskDescription, responseBody.description)
        assertEquals(userId, responseBody.assigneeId)
        assertEquals(userId, responseBody.creatorId)
    }

    //
    @Test
    fun `test get task by id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = createResponse.body<TaskResponse>()
        val taskId = createResponseBody.id

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Get the task by ID
        val response = client.get(Tasks.Id(taskId = taskId)) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the response contains the correct task
        assertEquals(taskId, responseBody.id)
        assertEquals("Test Task", responseBody.title)
        assertEquals("Test Description", responseBody.description)
    }

    @Test
    fun `test get task by id - not found`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Get a non-existent task
        val response = client.get(Tasks.Id(taskId = "00000000-0000-0000-0000-000000000000")) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response is not found
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    //
    @Test
    fun `test update task`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Original Title",
                    description = "Original Description",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = createResponse.body<TaskResponse>()
        val taskId = createResponseBody.id

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Update the task
        val response = client.put(Tasks.Id(taskId = taskId)) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskUpdateRequest(
                    title = "Updated Title",
                    description = "Updated Description",
                    status = TaskStatus.IN_PROGRESS,
                    priority = Priority.HIGH,
                    dueDate = LocalDateTime.parse("2023-12-31T23:59:59"),
                    assigneeId = userId
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the task was updated
        assertEquals("Updated Title", responseBody.title)
        assertEquals("Updated Description", responseBody.description)
        assertEquals(TaskStatus.IN_PROGRESS, responseBody.status)
    }

    //
    @Test
    fun `test delete task`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Task to Delete",
                    description = "This task will be deleted",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = createResponse.body<TaskResponse>()
        val taskId = createResponseBody.id

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Delete the task
        val response = client.delete(Tasks.Id(taskId = taskId)) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Try to get the deleted task
        val getResponse = client.get(Tasks.Id(taskId = taskId)) {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the task is not found
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `test assign task`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Create test users
        val user1Id = createTestUser(
            email = "user1@example.com",
            displayName = "User 1"
        )

        val user2Id = createTestUser(
            email = "user2@example.com",
            displayName = "User 2"
        )

        // Create a task assigned to user1
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = user1Id,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = createResponse.body<TaskResponse>()
        val taskId = createResponseBody.id

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Assign the task to user2
        val taskIdResource = Tasks.Id(taskId = taskId)
        val response = client.post(Tasks.Id.Assign(taskIdResource)) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(TaskAssignRequest(user2Id))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the task was assigned to user2
        assertEquals(user2Id, responseBody.assigneeId)
    }

    @Test
    fun `test change task status`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        JwtConfig.init(application.property<DomainJwtConfig>("ktor.jwt"))

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post(Tasks()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = userId,
                    priority = Priority.MEDIUM,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val taskId = createResponse.body<TaskResponse>().id

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Change the task status
        val taskStatusResource = Tasks.Id(taskId = taskId)
        val response = client.post(Tasks.Id.Status(taskStatusResource)) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            setBody(TaskStatusChangeRequest(TaskStatus.DONE.name))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<TaskResponse>()

        // Verify the task status was changed
        assertEquals(TaskStatus.DONE, responseBody.status)
    }

    //
    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}
