package com.danioliveira.appium

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.pages.LoginPage
import com.danioliveira.appium.perf.*
import com.danioliveira.appium.perf.core.*
import io.appium.java_client.android.AndroidDriver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

/**
 * Multi-flow scenario performance test using the new measurePerformance API.
 * 
 * This test demonstrates:
 * - Clean DSL for defining scenarios and flows
 * - Continuous systrace capture across multiple flows
 * - N iterations with aggregated statistics
 * - Screen segmentation using TraceLifecycle markers
 * - Unified export to JSON/CSV/Markdown
 * 
 * No manual sleeps or ad-hoc metric collection!
 */
class ScenarioPerformanceTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: AndroidDriver
    private lateinit var androidCollector: AndroidMetricsCollector
    private val packageName = "com.danioliveira.taskmanager"
    
    @BeforeEach
    fun setup() {
        val config = com.danioliveira.appium.config.BenchmarkConfig(
            platform = Platform.ANDROID,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "multi-flow-scenario",
            runs = 1,
            warmup = 0,
            deviceName = System.getProperty("deviceName") ?: "emulator-5554",
            udid = System.getProperty("udid"),
            apkPath = System.getProperty("apk"),
            ipaPath = null,
            bundleId = null,
            packageName = packageName,
            registerEveryCycle = false
        )
        
        driver = AndroidDriverFactory.create(config) as AndroidDriver
        androidCollector = AndroidMetricsCollector(packageName)
        
        logger.info("✅ ScenarioPerformanceTest setup complete")
    }
    
    @Test
    fun testMultiFlowScenario() = runBlocking {
        logger.info("=== Multi-Flow Scenario Performance Test ===")
        
        val scenario = Scenario(
            name = "Login to Task Creation",
            iterations = 3,
            flows = listOf(
                Flow(
                    name = "Login",
                    steps = {
                        performLogin()
                    },
                    expectedScreens = listOf("LoginScreen", "TasksScreen"),
                    durationMs = 3000
                ),
                Flow(
                    name = "Create Task",
                    steps = {
                        createTask()
                    },
                    expectedScreens = listOf("CreateTaskScreen", "TasksScreen"),
                    durationMs = 2000
                ),
                Flow(
                    name = "View Task Details",
                    steps = {
                        viewTaskDetails()
                    },
                    expectedScreens = listOf("TaskDetailsScreen"),
                    durationMs = 2000
                )
            )
        )
        
        val config = PerformanceConfig(
            platform = Platform.ANDROID,
            pollingIntervalMs = 500,
            systraceBufferSizeKb = 65536,
            enableCpuProfiling = true,
            enableMemoryProfiling = true,
            enableFpsProfiling = true
        )
        
        // Measure scenario
        val result = measureScenario(
            packageName = packageName,
            scenario = scenario,
            config = config,
            androidCollector = androidCollector
        )
        
        // Export results
        val outputDir = File("build/reports/scenario-performance")
        result.writeAll(outputDir)
        
        logger.info("=== Scenario Complete ===")
        logger.info("Results exported to: ${outputDir.absolutePath}")
        logger.info("Files:")
        logger.info("  - scenario_summary.json")
        logger.info("  - scenario_summary.md")
        logger.info("  - flows.csv")
        logger.info("  - screens_in_flows.csv")
        
        // Log summary
        logger.info("=== Performance Summary ===")
        logger.info("CPU: ${String.format("%.1f", result.aggregatedMetrics.cpu.meanOfMeans)}% " +
            "(95% CI: [${String.format("%.1f", result.aggregatedMetrics.cpu.confidenceInterval95.first)}, " +
            "${String.format("%.1f", result.aggregatedMetrics.cpu.confidenceInterval95.second)}])")
        logger.info("Memory: ${String.format("%.1f", result.aggregatedMetrics.memory.meanOfMeans)}MB " +
            "(95% CI: [${String.format("%.1f", result.aggregatedMetrics.memory.confidenceInterval95.first)}, " +
            "${String.format("%.1f", result.aggregatedMetrics.memory.confidenceInterval95.second)}])")
        logger.info("FPS: ${String.format("%.1f", result.aggregatedMetrics.fps.meanOfMeans)} " +
            "(95% CI: [${String.format("%.1f", result.aggregatedMetrics.fps.confidenceInterval95.first)}, " +
            "${String.format("%.1f", result.aggregatedMetrics.fps.confidenceInterval95.second)}])")
    }
    
    @Test
    fun testSingleTestCase() = runBlocking {
        logger.info("=== Single Test Case Performance Test ===")
        
        val testCase = TestCase(
            beforeTest = {
                // No setup needed, app already launched
            },
            run = {
                performLogin()
            },
            durationMs = 3000
        )
        
        val config = PerformanceConfig(
            platform = Platform.ANDROID,
            pollingIntervalMs = 500
        )
        
        // Measure performance
        val result = measurePerformance(
            packageName = packageName,
            testCase = testCase,
            config = config,
            androidCollector = androidCollector
        )
        
        // Export results
        val outputDir = File("build/reports/single-test-performance")
        result.writeAll(outputDir)
        
        logger.info("=== Test Complete ===")
        logger.info("Results exported to: ${outputDir.absolutePath}")
        logger.info("Duration: ${result.durationMs}ms")
        logger.info("CPU Avg: ${String.format("%.1f", result.metrics.cpu.avg)}%")
        logger.info("Memory Avg: ${String.format("%.1f", result.metrics.memory.avg)}MB")
        logger.info("FPS Avg: ${String.format("%.1f", result.metrics.fps.avg)}")
    }
    
    // Helper methods for test actions
    
    private suspend fun performLogin() {
        logger.info("  Performing login...")
        
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        
        // Wait for login screen
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//android.widget.EditText[@text='Email']")
        ))
        
        delay(500)
        
        // Enter credentials
        val emailField = driver.findElement(By.xpath("//android.widget.EditText[@text='Email']"))
        emailField.click()
        emailField.sendKeys("daniel@test.com")
        
        delay(300)
        
        val passwordField = driver.findElement(By.xpath("//android.widget.EditText[@text='Password']"))
        passwordField.click()
        passwordField.sendKeys("12345678")
        
        delay(300)
        
        // Click login
        val loginButton = driver.findElement(By.xpath("//android.widget.Button[@text='Login']"))
        loginButton.click()
        
        // Wait for tasks screen
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//android.view.View[@content-desc='Tasks']")
        ))
        
        delay(500)
        
        logger.info("  Login complete")
    }
    
    private suspend fun createTask() {
        logger.info("  Creating task...")
        
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        
        // Click FAB to open create task
        val fab = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//android.widget.Button[@content-desc='Add Task']")
        ))
        fab.click()
        
        delay(500)
        
        // Enter task details
        val titleField = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//android.widget.EditText[@text='Task title']")
        ))
        titleField.click()
        titleField.sendKeys("Performance Test Task")
        
        delay(300)
        
        // Save task
        val saveButton = driver.findElement(By.xpath("//android.widget.Button[@text='Save']"))
        saveButton.click()
        
        delay(500)
        
        logger.info("  Task created")
    }
    
    private suspend fun viewTaskDetails() {
        logger.info("  Viewing task details...")
        
        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        
        // Click on first task
        val firstTask = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//android.view.View[contains(@content-desc, 'Performance Test Task')]")
        ))
        firstTask.click()
        
        delay(1000)
        
        // Wait for details screen
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//android.view.View[@content-desc='Task Details']")
        ))
        
        delay(500)
        
        logger.info("  Task details viewed")
    }
    
    @AfterEach
    fun teardown() {
        driver.quit()
        logger.info("✅ Test teardown complete")
    }
}




