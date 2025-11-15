package com.danioliveira.appium

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.pages.LoginPage
import com.danioliveira.appium.pages.TasksPage
import com.danioliveira.appium.perf.core.Action
import com.danioliveira.appium.perf.core.FlowWithActions
import com.danioliveira.appium.perf.core.PerformanceConfig
import com.danioliveira.appium.perf.core.Scenario
import com.danioliveira.appium.perf.measureScenario
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Example test demonstrating FlowWithActions integration.
 * 
 * This test shows how to use the new action tracking feature to measure
 * performance of individual user interactions within a flow.
 */
class LoginWithActionsTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var androidCollector: AndroidMetricsCollector
    private val packageName = System.getProperty("packageName") ?: "com.danioliveira.taskmanager"

    @BeforeEach
    fun setup() {
        val config = com.danioliveira.appium.config.BenchmarkConfig(
            platform = Platform.ANDROID,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "login-with-actions",
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

        driver = AndroidDriverFactory.create(config)
        androidCollector = AndroidMetricsCollector(packageName)
        logger.info("✅ LoginWithActionsTest setup complete")
    }

    @Test
    fun testLoginFlowWithActionTracking() = runBlocking {
        logger.info("=== Starting Login Flow with Action Tracking ===")

        val loginPage = LoginPage(driver, Platform.ANDROID)
        val tasksPage = TasksPage(driver, Platform.ANDROID)

        // Define scenario with explicit action tracking
        val scenario = Scenario(
            name = "LoginWithActions",
            iterations = 3,
            flows = listOf(
                FlowWithActions(
                    name = "Login",
                    actions = listOf(
                        Action("WaitForLoginScreen") {
                            loginPage.waitForLoginScreen()
                            delay(500)
                        },
                        Action("EnterEmail") {
                            loginPage.enterEmail("daniel@test.com")
                            delay(300)
                        },
                        Action("EnterPassword") {
                            loginPage.enterPassword("12345678")
                            delay(300)
                        },
                        Action("ClickLoginButton") {
                            loginPage.clickLogin()
                            delay(2000) // Wait for navigation
                        },
                        Action("WaitForTasksScreen") {
                            tasksPage.waitForTasksList()
                            delay(500)
                        }
                    ),
                    expectedScreens = listOf("LoginScreen", "TasksScreen")
                )
            )
        )

        val config = PerformanceConfig(
            platform = Platform.ANDROID,
            enableCpuProfiling = true,
            enableMemoryProfiling = true,
            enableFpsProfiling = true,
            pollingIntervalMs = 500
        )

        // Measure scenario with action tracking
        val result = measureScenario(
            packageName = packageName,
            scenario = scenario,
            config = config,
            androidCollector = androidCollector
        )

        // Export results
        val outputDir = File("build/reports/login-with-actions")
        result.writeAll(outputDir)

        logger.info("=== Results Summary ===")
        logger.info("Scenario: ${result.scenarioName}")
        logger.info("Iterations: ${result.iterations}")
        logger.info("Total flows: ${result.flowResults.size}")
        
        // Log action-level metrics
        result.flowResults.forEach { flow ->
            logger.info("Flow: ${flow.flowName}, Iteration: ${flow.iteration}")
            flow.actionResults.forEach { action ->
                logger.info("  Action: ${action.actionName}")
                logger.info("    Duration: ${action.durationMs}ms")
                logger.info("    CPU: ${String.format("%.1f", action.metrics.cpu.avg)}% " +
                    "(peak: ${String.format("%.1f", action.metrics.cpu.peak)}%, " +
                    "source: ${action.metrics.sources["cpu"]}, " +
                    "samples: ${action.metrics.cpu.samples})")
                logger.info("    Memory: ${action.metrics.memoryMb}MB " +
                    "(Δ${action.metrics.memoryDeltaMb}MB)")
                logger.info("    FPS: ${String.format("%.1f", action.metrics.fps)} " +
                    "(source: ${action.metrics.sources["fps"]})")
            }
        }

        logger.info("✅ Test Complete. Results exported to: ${outputDir.absolutePath}")
        logger.info("   - scenario_summary.json")
        logger.info("   - scenario_summary.md")
        logger.info("   - flows.csv")
        logger.info("   - screens_in_flows.csv")
        logger.info("   - actions.csv (NEW!)")
    }

    @AfterEach
    fun teardown() {
        driver.quit()
        logger.info("✅ Driver quit")
    }
}

