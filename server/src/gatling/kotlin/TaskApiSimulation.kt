import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

class TaskApiSimulation : Simulation() {

    // Use AtomicLong for thread-safe unique ID generation
    private val idCounter = AtomicLong(System.currentTimeMillis()) // Start with a timestamp-based unique value

    // Configure JSON serialization with proper settings
    private val jsonConvert = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    // Feeder for dynamic user data
    private val userFeeder = generateSequence {
        val userId = idCounter.getAndIncrement()
        mapOf(
            "email" to "user_$userId@test.com",
            "password" to "password123",
            "displayName" to "Test User $userId"
        )
    }.iterator()

    // Base configuration
    private val baseUrl = "http://localhost:8081"
    private val httpProtocol = http
        .baseUrl(baseUrl)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/Performance Test")

    // === HTTP Requests ===

    // Register User
    private val registerRequest = exec { session ->
        val email = session.get<String>("email")!!
        val password = session.get<String>("password")!!
        val displayName = session.get<String>("displayName")!!

        val registerModel = RegisterRequest(
            email = email,
            password = password,
            displayName = displayName
        )

        session.set("registerRequestJson", jsonConvert.encodeToString(RegisterRequest.serializer(), registerModel))
    }.exec(
        http("Register User")
            .post("/api/auth/register")
            .body(StringBody("#{registerRequestJson}"))
            .check(status().`is`(201)) // Check for 201 Created status
        // Optional: Check response body if needed
    )

    // Login User
    private val loginRequest = exec { session ->
        val email = session.get<String>("email")!!
        val password = session.get<String>("password")!!

        val loginModel = LoginRequest(
            email = email,
            password = password
        )

        session.set("loginRequestJson", jsonConvert.encodeToString(LoginRequest.serializer(), loginModel))
    }.exec(
        http("Login Request")
            .post("/api/auth/login")
            .body(StringBody("#{loginRequestJson}"))
            .check(
                status().`is`(200),
                jsonPath("$.token").saveAs("authToken") // Save token from response
            )
    )

    // Create Task with dynamic timestamp and task ID
    private val createTaskRequest = exec { session ->
        // Generate a unique task ID and timestamp for each request
        val taskId = idCounter.getAndIncrement()
        val timestamp = System.currentTimeMillis()
        val email = session.get<String>("email")!!

        val taskModel = TaskCreateRequest(
            title = "Task $taskId for $email",
            description = "Load test task created at $timestamp",
            status = "TODO",
            dueDate = null,
            projectId = null,
            assigneeId = null
        )

        session.set("taskRequestJson", jsonConvert.encodeToString(TaskCreateRequest.serializer(), taskModel))
    }.exec(
        http("Create Task")
            .post("/api/tasks")
            .header("Authorization", "Bearer #{authToken}") // Use saved token
            .body(StringBody("#{taskRequestJson}"))
            .check(status().`is`(201))
    )

    // Get Tasks
    private val getTasksRequest = exec(
        http("Get All Tasks")
            .get("/api/tasks")
            .header("Authorization", "Bearer #{authToken}")
            .check(status().`is`(200))
    )

    // === Scenarios ===

    // Scenario: Register, Login, Create Task, Get Tasks
    private val userJourneyScenario = scenario("User Full Journey")
        .feed(userFeeder) // Feed dynamic user data
        .exec(registerRequest)
        .pause(1) // Pause between requests
        .exec(loginRequest)
        .pause(1)
        .exec(createTaskRequest)
        .pause(1)
        .exec(getTasksRequest)

    // Scenario: Login and Get Tasks (simulates read-heavy load)
    private val readOnlyScenario = scenario("Read Only Journey")
        .feed(userFeeder) // Feed dynamic user data (might reuse, depends on test goal)
        .exec(loginRequest) // Assumes users from feeder might exist or handle login failure
        .pause(1)
        .exec(getTasksRequest)


    // === Load Test Setup ===
    init {
        setUp(
            // User Journey Scenario (Registration + Write + Read)
            // Gradually increase load over 30 mins
            userJourneyScenario.injectOpen(
                // Ramp up users per second over the first 10 mins
                rampUsersPerSec(1.0).to(5.0).during(Duration.ofMinutes(10)),
                // Maintain and slightly increase load over the next 10 mins
                constantUsersPerSec(5.0).during(Duration.ofMinutes(5)),
                rampUsersPerSec(5.0).to(10.0).during(Duration.ofMinutes(5)),
                // Push the load higher in the final 10 mins
                constantUsersPerSec(10.0).during(Duration.ofMinutes(5)),
                rampUsersPerSec(10.0).to(15.0).during(Duration.ofMinutes(5))
            ),

            // Read Only Scenario (Read Heavy)
            // Inject more users for read operations, also increasing over time
            readOnlyScenario.injectOpen(
                // Ramp up users per second over the first 10 mins (higher rate than write)
                rampUsersPerSec(2.0).to(10.0).during(Duration.ofMinutes(10)),
                // Maintain and slightly increase load over the next 10 mins
                constantUsersPerSec(10.0).during(Duration.ofMinutes(5)),
                rampUsersPerSec(10.0).to(20.0).during(Duration.ofMinutes(5)),
                // Push the read load higher in the final 10 mins
                constantUsersPerSec(20.0).during(Duration.ofMinutes(5)),
                rampUsersPerSec(20.0).to(30.0).during(Duration.ofMinutes(5))
            )

        ).protocols(httpProtocol)
            .maxDuration(Duration.ofMinutes(30)) // Ensure total duration is 30 mins
            .assertions(
                // Example Assertions: Check overall success rate and response times
                global().successfulRequests().percent().gt(95.0),
                global().responseTime().max().lt(1000) // Max response time < 1000 ms
                // Add more specific assertions per request if needed
            )
    }
}
