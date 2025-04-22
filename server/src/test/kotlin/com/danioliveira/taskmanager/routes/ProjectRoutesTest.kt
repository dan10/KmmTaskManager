package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.*
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.domain.AppConfig
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
import kotlin.test.assertTrue

class ProjectRoutesTest : KoinTest {

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
    fun `test get projects by owner - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a test project
        val projectName = "Test Project"
        val projectDescription = "Test Description"

        // Create a project using the API
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            jsonBody(ProjectCreateRequest(projectName, projectDescription))
        }

        // Verify the project was created successfully
        assertEquals(HttpStatusCode.OK, createResponse.status)

        // Get projects by owner
        val response = client.get("/projects") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains the created project
        assertNotNull(items)
        assertEquals(1, items!!.size)
        assertEquals(projectName, items[0].jsonObject["name"]?.jsonPrimitive?.content)
        assertEquals(projectDescription, items[0].jsonObject["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get projects by owner - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Get projects without authentication
        val response = client.get("/projects")

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test get all projects`() = testApplication {
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

        // Create projects for each user
        client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            jsonBody(ProjectCreateRequest("Project 1", "Description 1"))
        }

        client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user2Id, "user2@example.com"))
            jsonBody(ProjectCreateRequest("Project 2", "Description 2"))
        }

        // Get all projects
        val response = client.get("/projects/all") {
            withAuth(generateTestToken(user1Id, "user1@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val items = responseBody["items"]?.jsonArray

        // Verify the response contains both projects
        assertNotNull(items)
        assertEquals(2, items!!.size)
    }

    @Test
    fun `test create project - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val projectName = "New Project"
        val projectDescription = "New Description"

        val response = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            jsonBody(ProjectCreateRequest(projectName, projectDescription))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the response contains the created project
        assertEquals(projectName, responseBody["name"]?.jsonPrimitive?.content)
        assertEquals(projectDescription, responseBody["description"]?.jsonPrimitive?.content)
        assertEquals(userId, responseBody["ownerId"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test create project - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Try to create a project without authentication
        val response = client.post("/projects") {
            contentType(ContentType.Application.Json)
            jsonBody(ProjectCreateRequest("New Project", "New Description"))
        }

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test get project by id`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Get the project by ID
        val response = client.get("/projects/$projectId") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        // Verify the response contains the correct project
        assertEquals(projectId, responseBody["id"]?.jsonPrimitive?.content)
        assertEquals("Test Project", responseBody["name"]?.jsonPrimitive?.content)
        assertEquals("Test Description", responseBody["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test get project by id - not found`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Get a non-existent project
        val response = client.get("/projects/00000000-0000-0000-0000-000000000000") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response is not found
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test update project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner_project@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner_projec@example.com"))
            jsonBody(ProjectCreateRequest("Original Name", "Original Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Update the project
        val response = client.put("/projects/$projectId") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            jsonBody(ProjectUpdateRequest("Updated Name", "Updated Description"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Get the updated project
        val getResponse = client.get("/projects/$projectId") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Parse the response body
        val getResponseBody = Json.parseToJsonElement(getResponse.bodyAsText()).jsonObject

        // Verify the project was updated
        assertEquals("Updated Name", getResponseBody["name"]?.jsonPrimitive?.content)
        assertEquals("Updated Description", getResponseBody["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test delete project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Project to Delete", "This project will be deleted"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Delete the project
        val response = client.delete("/projects/$projectId") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Try to get the deleted project
        val getResponse = client.get("/projects/$projectId") {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the project is not found
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `test assign user to project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create test users
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project
        val response = client.post("/projects/$projectId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(userId))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Get users assigned to the project
        val usersResponse = client.get("/projects/$projectId/users") {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }

        // Parse the response body
        val usersResponseBody = Json.parseToJsonElement(usersResponse.bodyAsText()).jsonArray

        // Verify the user is assigned to the project
        assertEquals(1, usersResponseBody.size)
        assertEquals(userId, usersResponseBody[0].jsonPrimitive.content)
    }

    @Test
    fun `test remove user from project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create test users
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project
        client.post("/projects/$projectId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(userId))
        }

        // Remove the user from the project
        val response = client.delete("/projects/$projectId/assign/$userId") {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Get users assigned to the project
        val usersResponse = client.get("/projects/$projectId/users") {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }

        // Parse the response body
        val usersResponseBody = Json.parseToJsonElement(usersResponse.bodyAsText()).jsonArray

        // Verify the user is no longer assigned to the project
        assertEquals(0, usersResponseBody.size)
    }

    @Test
    fun `test get users by project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create test users
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        val user1Id = createTestUser(
            email = "user1@example.com",
            displayName = "User 1"
        )

        val user2Id = createTestUser(
            email = "user2@example.com",
            displayName = "User 2"
        )

        // Create a project
        val createResponse = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign users to the project
        client.post("/projects/$projectId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(user1Id))
        }

        client.post("/projects/$projectId/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(user2Id))
        }

        // Get users assigned to the project
        val response = client.get("/projects/$projectId/users") {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonArray

        // Verify the response contains both users
        assertEquals(2, responseBody.size)
        assertTrue(responseBody.any { it.jsonPrimitive.content == user1Id })
        assertTrue(responseBody.any { it.jsonPrimitive.content == user2Id })
    }

    @Test
    fun `test get projects by user`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        // Create test users
        val ownerId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        val userId = createTestUser(
            email = "user@example.com",
            displayName = "Project User"
        )

        // Create projects
        val createResponse1 = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Project 1", "Description 1"))
        }

        val createResponse2 = client.post("/projects") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectCreateRequest("Project 2", "Description 2"))
        }

        // Get the project IDs from the responses
        val createResponseBody1 = Json.parseToJsonElement(createResponse1.bodyAsText()).jsonObject
        val projectId1 = createResponseBody1["id"]?.jsonPrimitive?.content

        val createResponseBody2 = Json.parseToJsonElement(createResponse2.bodyAsText()).jsonObject
        val projectId2 = createResponseBody2["id"]?.jsonPrimitive?.content

        // Verify the project IDs are not null
        assertNotNull(projectId1)
        assertNotNull(projectId2)

        // Assign the user to the projects
        client.post("/projects/$projectId1/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(userId))
        }

        client.post("/projects/$projectId2/assign") {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            jsonBody(ProjectAssignRequest(userId))
        }

        // Get projects the user is assigned to
        val response = client.get("/projects/user/$userId") {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = Json.parseToJsonElement(response.bodyAsText()).jsonArray

        // Verify the response contains both projects
        assertEquals(2, responseBody.size)
        assertTrue(responseBody.any { it.jsonPrimitive.content == projectId1 })
        assertTrue(responseBody.any { it.jsonPrimitive.content == projectId2 })
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}
