import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.domain.Priority
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

    // Configurable base URL (defaults to Kotlin server)
    private val baseUrl = System.getProperty("gatling.baseUrl", "http://localhost:8081")

    // Test mode configuration
    private val testMode = System.getProperty("gatling.test.mode", "quick")

    // Load test configuration based on mode
    private val loadConfig = when (testMode) {
        "long" -> LoadConfig(
            userJourneyRampFrom = 10.0,
            userJourneyRampTo = 100.0,
            readOnlyRampFrom = 20.0,
            readOnlyRampTo = 200.0,
            duration = Duration.ofMinutes(30),
            maxDuration = Duration.ofMinutes(35),
            maxResponseTime = 2000
        )

        "stress" -> LoadConfig(
            userJourneyRampFrom = 50.0,
            userJourneyRampTo = 500.0,
            readOnlyRampFrom = 100.0,
            readOnlyRampTo = 1000.0,
            duration = Duration.ofMinutes(10),
            maxDuration = Duration.ofMinutes(15),
            maxResponseTime = 10000
        )

        else -> LoadConfig( // "quick" mode
            userJourneyRampFrom = 1.0,
            userJourneyRampTo = 10.0,
            readOnlyRampFrom = 1.0,
            readOnlyRampTo = 10.0,
            duration = Duration.ofMinutes(2),
            maxDuration = Duration.ofMinutes(3),
            maxResponseTime = 5000
        )
    }

    data class LoadConfig(
        val userJourneyRampFrom: Double,
        val userJourneyRampTo: Double,
        val readOnlyRampFrom: Double,
        val readOnlyRampTo: Double,
        val duration: Duration,
        val maxDuration: Duration,
        val maxResponseTime: Int
    )

    // Feeder for dynamic user data
    private val userFeeder = generateSequence {
        val userId = idCounter.getAndIncrement()
        mapOf(
            "email" to "user_$userId@test.com",
            "password" to "password123",
            "displayName" to "Test User $userId"
        )
    }.iterator()

    // Base configuration with configurable URL
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
            .check(status().`is`(200))
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
            priority = Priority.MEDIUM,
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
            .check(status().`is`(200))
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

    // Scenario: Register, Login and Get Tasks (simulates read-heavy load)
    private val readOnlyScenario = scenario("Read Only Journey")
        .feed(userFeeder)
        .exec(registerRequest)
        .pause(1)
        .exec(loginRequest)
        .pause(1)
        .exec(getTasksRequest)

    // === Load Test Setup ===
    init {
        println("Running Gatling test in '$testMode' mode")
        println("Target URL: $baseUrl")
        println("User Journey: ${loadConfig.userJourneyRampFrom} to ${loadConfig.userJourneyRampTo} users/sec")
        println("Read Only: ${loadConfig.readOnlyRampFrom} to ${loadConfig.readOnlyRampTo} users/sec")
        println("Duration: ${loadConfig.duration}")
        
        setUp(
            // User Journey Scenario (Registration + Write + Read)
            userJourneyScenario.injectOpen(
                rampUsersPerSec(loadConfig.userJourneyRampFrom).to(loadConfig.userJourneyRampTo)
                    .during(loadConfig.duration)
            ),
            // Read Only Scenario (Read Heavy)
            readOnlyScenario.injectOpen(
                rampUsersPerSec(loadConfig.readOnlyRampFrom).to(loadConfig.readOnlyRampTo).during(loadConfig.duration)
            )
        ).protocols(httpProtocol)
            .maxDuration(loadConfig.maxDuration)
            .assertions(
                // Success rate assertions
                global().successfulRequests().percent().gt(95.0),
                // Response time assertions
                global().responseTime().max().lt(loadConfig.maxResponseTime)
            )
    }
}
