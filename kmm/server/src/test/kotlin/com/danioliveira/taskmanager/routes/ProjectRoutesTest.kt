package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.api.request.ProjectAssignRequest
import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.response.ProjectResponse
import com.danioliveira.taskmanager.api.routes.Projects
import com.danioliveira.taskmanager.api.routes.UserProjects
import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.createTestUser
import com.danioliveira.taskmanager.generateTestToken
import com.danioliveira.taskmanager.getTestModule
import com.danioliveira.taskmanager.withAuth
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectRoutesTest : KoinTest {

    @Before
    fun setUp() = runBlocking {
        // Initialize the H2 database
        TestDatabase.init()

        // Start Koin with the test module
        startKoin {
            modules(getTestModule())
        }

        // Initialize JwtConfig directly for tests
        JwtConfig.init(
            com.danioliveira.taskmanager.domain.JwtConfig(
                secret = "test_jwt_secret",
                issuer = "taskit",
                audience = "taskit_audience",
                realm = "taskit_realm",
                validityMs = 3_600_000
            )
        )
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
    fun `test get projects by owner - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
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
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            setBody(ProjectCreateRequest(projectName, projectDescription))
        }

        // Verify the project was created successfully
        assertEquals(HttpStatusCode.OK, createResponse.status)

        // Get projects by owner
        val response = client.get(UserProjects()) {
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

    //
    @Test
    fun `test get projects by owner - unauthenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Get projects without authentication
        val response = client.get(UserProjects())

        // Verify the response is unauthorized
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test get all projects`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
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
        client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(ProjectCreateRequest("Project 1", "Description 1"))
        }

        client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user2Id, "user2@example.com"))
            setBody(ProjectCreateRequest("Project 2", "Description 2"))
        }

        // Create another project for user1 and then get user1 projects
        client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(user1Id, "user1@example.com"))
            setBody(ProjectCreateRequest("Project 1B", "Description 1B"))
        }

        val response = client.get(UserProjects()) {
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

    //
    @Test
    fun `test create project - authenticated`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val projectName = "New Project"
        val projectDescription = "New Description"

        val response = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            setBody(ProjectCreateRequest(projectName, projectDescription))
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

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Try to create a project without authentication
        val response = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            setBody(ProjectCreateRequest("New Project", "New Description"))
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

        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            setBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = createResponse.body<ProjectResponse>()
        val projectId = createResponseBody.id

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Get the project by ID
        val response = client.get(Projects.Id(projectId = projectId)) {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Parse the response body
        val responseBody = response.body<ProjectResponse>()

        // Verify the response contains the correct project
        assertEquals(projectId, responseBody.id)
        assertEquals("Test Project", responseBody.name)
        assertEquals("Test Description", responseBody.description)
    }

    @Test
    fun `test get project by id - not found`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Get a non-existent project
        val response = client.get(Projects.Id(projectId = "00000000-0000-0000-0000-000000000000")) {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response is not found
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    //
    @Test
    fun `test update project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner_project@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner_projec@example.com"))
            setBody(ProjectCreateRequest("Original Name", "Original Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Update the project
        val response = client.put(Projects.Id(projectId = projectId!!)) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            setBody(ProjectUpdateRequest("Updated Name", "Updated Description"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.OK, response.status)

        // Get the updated project
        val getResponse = client.get(Projects.Id(projectId = projectId)) {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Parse the response body
        val getResponseBody = Json.parseToJsonElement(getResponse.bodyAsText()).jsonObject

        // Verify the project was updated
        assertEquals("Updated Name", getResponseBody["name"]?.jsonPrimitive?.content)
        assertEquals("Updated Description", getResponseBody["description"]?.jsonPrimitive?.content)
    }

    //
    @Test
    fun `test delete project`() = testApplication {
        // Set up the test environment
        environment {
            config = ApplicationConfig("application_test.conf")
        }

        val client = createClient {
            install(Resources)
        }

        // Create a test user
        val userId = createTestUser(
            email = "owner@example.com",
            displayName = "Project Owner"
        )

        // Create a project
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(userId, "owner@example.com"))
            setBody(ProjectCreateRequest("Project to Delete", "This project will be deleted"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Delete the project
        val response = client.delete(Projects.Id(projectId = projectId!!)) {
            withAuth(generateTestToken(userId, "owner@example.com"))
        }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Try to get the deleted project
        val getResponse = client.get(Projects.Id(projectId = projectId)) {
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

        val client = createClient {
            install(Resources)
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
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project
        val response = client.post(Projects.Id.Assign(Projects.Id(projectId = projectId!!))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(userId))
        }

        // Verify the response
        assertEquals(HttpStatusCode.Created, response.status)

        // Get users assigned to the project
        val usersResponse = client.get(Projects.Id.Users(Projects.Id(projectId = projectId))) {
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

        val client = createClient {
            install(Resources)
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
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign the user to the project
        client.post(Projects.Id.Assign(Projects.Id(projectId = projectId!!))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(userId))
        }

        // Remove the user from the project
        val response =
            client.delete(Projects.Id.AssignUser(Projects.Id(projectId = projectId), userId)) {
                withAuth(generateTestToken(ownerId, "owner@example.com"))
            }

        // Verify the response
        assertEquals(HttpStatusCode.NoContent, response.status)

        // Get users assigned to the project
        val usersResponse = client.get(Projects.Id.Users(Projects.Id(projectId = projectId))) {
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

        val client = createClient {
            install(Resources)
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
        val createResponse = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectCreateRequest("Test Project", "Test Description"))
        }

        // Get the project ID from the response
        val createResponseBody = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject
        val projectId = createResponseBody["id"]?.jsonPrimitive?.content

        // Verify the project ID is not null
        assertNotNull(projectId)

        // Assign users to the project
        client.post(Projects.Id.Assign(Projects.Id(projectId = projectId!!))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(user1Id))
        }

        client.post(Projects.Id.Assign(Projects.Id(projectId = projectId))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(user2Id))
        }

        // Get users assigned to the project
        val response = client.get(Projects.Id.Users(Projects.Id(projectId = projectId))) {
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

        val client = createClient {
            install(Resources)
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
        val createResponse1 = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectCreateRequest("Project 1", "Description 1"))
        }

        val createResponse2 = client.post(Projects()) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectCreateRequest("Project 2", "Description 2"))
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
        client.post(Projects.Id.Assign(Projects.Id(projectId = projectId1!!))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(userId))
        }

        client.post(Projects.Id.Assign(Projects.Id(projectId = projectId2!!))) {
            contentType(ContentType.Application.Json)
            withAuth(generateTestToken(ownerId, "owner@example.com"))
            setBody(ProjectAssignRequest(userId))
        }
        // Verify user is listed in each project's users endpoint
        val usersResp1 = client.get(Projects.Id.Users(Projects.Id(projectId = projectId1))) {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }
        val users1 = Json.parseToJsonElement(usersResp1.bodyAsText()).jsonArray
        assertTrue(users1.any { it.jsonPrimitive.content == userId })

        val usersResp2 = client.get(Projects.Id.Users(Projects.Id(projectId = projectId2))) {
            withAuth(generateTestToken(ownerId, "owner@example.com"))
        }
        val users2 = Json.parseToJsonElement(usersResp2.bodyAsText()).jsonArray
        assertTrue(users2.any { it.jsonPrimitive.content == userId })
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}
