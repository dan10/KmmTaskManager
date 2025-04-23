package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.*
import com.danioliveira.taskmanager.api.request.*
import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.domain.TaskStatus
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskRoutesTest : KoinTest {

    @Before
    fun setUp() {
        // Initialize the H2 database
        TestDatabase.init()

        // Start Koin with the test module
        startKoin {
            modules(getTestModule(ApplicationConfig("application_test.conf")))
        }

        // Initialize JwtConfig with appConfig from Koin
        val appConfig = inject<AppConfig>().value
        JwtConfig.init(appConfig)
    }

    @After
    fun tearDown() {
        // Clear the database after each test
        TestDatabase.clearDatabase()

        // Stop Koin
        stopKoin()
    }

    @Test
    fun `test get tasks - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
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
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Verify the task was created successfully
        assertEquals(HttpStatusCode.OK, createResponse.status)

        // Get tasks
        val response = client.get("/tasks") {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains the created task
        assertNotNull(items)
        assertEquals(1, items!!.size)
        assertEquals(taskTitle, items[0].jsonObject["title"]?.jsonPrimitive?.content)
        assertEquals(taskDescription, items[0].jsonObject["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get tasks - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Get tasks without authentication
        val response = client.get("/tasks")

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test get tasks by project id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a project
        val projectResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val projectResponseBody = Json.parseToJsonElement(projectResponse.bodyAsText()).jsonObject
        val projectId = projectResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project (needed for the assignee validation)
        client.post("/projects/$projectId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(mapOf("userId" to userId))
        }

        // Create a task for the project
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Project Task",
                    description = "Task for the project",
                    projectId = projectId,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Create a task without a project
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Non-Project Task",
                    description = "Task without a project",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get tasks for the project
        val response = client.get("/tasks?projectId=$projectId") {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains only the project task
        assertNotNull(items)
        assertEquals(1, items!!.size)
        assertEquals("Project Task", items[0].jsonObject["title"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get tasks by assignee id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
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
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "User 1 Task",
                    description = "Task for User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Create a task assigned to user2
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "User 2 Task",
                    description = "Task for User 2",
                    projectId = null,
                    assigneeId = user2Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get tasks assigned to user1
        val response = client.get("/tasks?assigneeId=$user1Id") {
            withAuth(generateTestToken(user1Id, "user1@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains only the task assigned to user1
        assertNotNull(items)
        assertEquals(1, items!!.size)
        assertEquals("User 1 Task", items[0].jsonObject["title"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get tasks by user`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
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
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Task 1",
                    description = "Task created by User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Task 2",
                    description = "Another task created by User 1",
                    projectId = null,
                    assigneeId = user1Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Create a task created by user2
        client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user2Id, "user2@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Task 3",
                    description = "Task created by User 2",
                    projectId = null,
                    assigneeId = user2Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get tasks created by user1
        val response = client.get("/tasks/user") {
            withAuth(generateTestToken(user1Id, "user1@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains only the tasks created by user1
        assertNotNull(items)
        assertEquals(2, items!!.size)
        assertTrue(items.any { it.jsonObject["title"]?.jsonPrimitive?.content == "Task 1" })
        assertTrue(items.any { it.jsonObject["title"]?.jsonPrimitive?.content == "Task 2" })
        assertFalse(items.any { it.jsonObject["title"]?.jsonPrimitive?.content == "Task 3" })
    }

    @Test
    fun `test create task - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val taskTitle = "New Task"
        val taskDescription = "New Description"

        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the response contains the created task
        assertEquals(taskTitle, responseBody["title"]?.jsonPrimitive?.content)
        assertEquals(taskDescription, responseBody["description"]?.jsonPrimitive?.content)
        assertEquals(userId, responseBody["assigneeId"]?.jsonPrimitive?.content)
        assertEquals(userId, responseBody["creatorId"]?.jsonPrimitive?.content)
        assertEquals(TaskStatus.TODO.name, responseBody["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test create task - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Try to create a task without authentication
        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            jsonBody(
                TaskCreateRequest(
                    title = "New Task",
                    description = "New Description",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test create task for current user`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task for the current user
        val taskTitle = "User Task"
        val taskDescription = "Task for the current user"

        val response = client.post("/tasks/user") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = taskTitle,
                    description = taskDescription,
                    projectId = null,
                    assigneeId = "some-other-user-id", // This should be ignored and replaced with the current user ID
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the response contains the created task with the current user as assignee
        assertEquals(taskTitle, responseBody["title"]?.jsonPrimitive?.content)
        assertEquals(taskDescription, responseBody["description"]?.jsonPrimitive?.content)
        assertEquals(userId, responseBody["assigneeId"]?.jsonPrimitive?.content) // Should be the current user ID
        assertEquals(userId, responseBody["creatorId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get task by id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Get the task by ID
        val response = client.get("/tasks/$taskId") {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the response contains the correct task
        assertEquals(taskId, responseBody["id"]?.jsonPrimitive?.content)
        assertEquals("Test Task", responseBody["title"]?.jsonPrimitive?.content)
        assertEquals("Test Description", responseBody["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get task by id - not found`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Get a non-existent task
        val response = client.get("/tasks/00000000-0000-0000-0000-000000000000") {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response is not found
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test update task`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Original Title",
                    description = "Original Description",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Update the task
        val response = client.put("/tasks/$taskId") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskUpdateRequest(
                    title = "Updated Title",
                    description = "Updated Description",
                    status = TaskStatus.IN_PROGRESS,
                    dueDate = "2023-12-31T23:59:59",
                    assigneeId = userId
                )
            )
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the task was updated
        assertEquals("Updated Title", responseBody["title"]?.jsonPrimitive?.content)
        assertEquals("Updated Description", responseBody["description"]?.jsonPrimitive?.content)
        assertEquals(TaskStatus.IN_PROGRESS.name, responseBody["status"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test delete task`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Task to Delete",
                    description = "This task will be deleted",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Delete the task
        val response = client.delete("/tasks/$taskId") {
            withAuth(generateTestToken(userId, "user@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Try to get the deleted task
        val getResponse = client.get("/tasks/$taskId") {
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
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = user1Id,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Assign the task to user2
        val response = client.post("/tasks/$taskId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(TaskAssignRequest(user2Id))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the task was assigned to user2
        assertEquals(user2Id, responseBody["assigneeId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test change task status`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Task User"
        )

        // Create a task
        val createResponse = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(
                TaskCreateRequest(
                    title = "Test Task",
                    description = "Test Description",
                    projectId = null,
                    assigneeId = userId,
                    status = TaskStatus.TODO.name,
                    dueDate = null
                )
            )
        }

        // Get the task ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val taskId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the task ID is not null
        assertNotNull(taskId)

        // Change the task status
        val response = client.post("/tasks/$taskId/status") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "user@example.com"))
            jsonBody(TaskStatusChangeRequest(TaskStatus.DONE.name))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the task status was changed
        assertEquals(TaskStatus.DONE.name, responseBody["status"]?.jsonPrimitive?.content)
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}
