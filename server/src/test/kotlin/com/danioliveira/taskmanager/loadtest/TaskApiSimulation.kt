package com.danioliveira.taskmanager.loadtest

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.time.Duration

class TaskApiSimulation : Simulation() {

    // Base configuration
    private val baseUrl = "http://localhost:8080"
    private val httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/Performance Test")

    // Test data
    private val testUser = """
        {
            "email": "test@example.com",
            "password": "password123"
        }
    """.trimIndent()

    private val testTask = """
        {
            "title": "Test Task",
            "description": "This is a test task created by the load test",
            "status": "TODO"
        }
    """.trimIndent()

    // Login HTTP request
    private val loginRequest = exec(
        http("Login Request")
            .post("/api/auth/login")
            .body(StringBody(testUser))
            .check(
                status().shouldBe(200),
                jsonPath("$.token").saveAs("authToken")
            )
    )

    // Create task HTTP request
    private val createTaskRequest = exec(
        http("Create Task")
            .post("/api/tasks")
            .header("Authorization", "Bearer #{authToken}")
            .body(StringBody(testTask))
            .check(status().shouldBe(201))
    )

    // Get tasks HTTP request
    private val getTasksRequest = exec(
        http("Get All Tasks")
            .get("/api/tasks")
            .header("Authorization", "Bearer #{authToken}")
            .check(status().shouldBe(200))
    )

    // Scenario definitions
    private val createTaskScenario = scenario("Create Task")
        .exec(loginRequest)
        .exec(createTaskRequest)

    private val getTasksScenario = scenario("Get Tasks")
        .exec(loginRequest)
        .exec(getTasksRequest)

    // Load test setup
    init {
        setUp(
            // Create task scenario: 
            // - Ramp up to 50 users over 5 minutes
            // - Maintain 50 users for 25 minutes
            createTaskScenario.injectOpen(
                rampUsers(50).during(Duration.ofMinutes(5)),
                constantUsersPerSec(50.0).during(Duration.ofMinutes(25))
            ),
            // Get tasks scenario:
            // - Ramp up to 100 users over 5 minutes
            // - Maintain 100 users for 25 minutes
            getTasksScenario.injectOpen(
                rampUsers(100).during(Duration.ofMinutes(5)),
                constantUsersPerSec(100.0).during(Duration.ofMinutes(25))
            )
        ).protocols(httpProtocol)
            .maxDuration(Duration.ofMinutes(30))
    }
}
