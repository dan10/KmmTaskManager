package com.danioliveira.appium

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.new.BaseScreen
import com.danioliveira.appium.new.LoginPage
import com.danioliveira.appium.new.TaskCreatePage
import com.danioliveira.appium.new.TasksPage
import com.danioliveira.appium.reporting.MetricsReporter
import com.danioliveira.appium.runner.BenchmarkRunner
import com.google.gson.GsonBuilder

import org.junit.jupiter.api.*
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Framework Comparison Test Orchestrator
 * 
 * Runs identical test flows against both CMP and Flutter apps,
 * collecting performance metrics for comparison.
 * 
 * Features:
 * - Configurable iterations (default: 15)
 * - Optional warmup runs (discarded)
 * - Per-action metrics collection
 * - Trace-based performance analysis
 * - Side-by-side comparison reports
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FrameworkComparisonTest {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    private val config = BenchmarkConfig.fromSystemProperties()
    private val runner = BenchmarkRunner(config)
    
    private val outputDir = File("build/reports/metrics")
    private val reporter = MetricsReporter(outputDir)

    @BeforeAll
    fun setup() {
        logger.info("🚀 Starting Framework Comparison Benchmark")
        logger.info("Config: $config")
        outputDir.mkdirs()
    }

    @Test
    @DisplayName("Run Single Iteration (Verification)")
    fun testSingleIteration() {
        logger.info("🧪 Running single iteration verification test")


        // Run KMM test
        val kmmMetrics = runner.runTestSuite(
            app = App.KMM,
            frameworkName = "Compose Multiplatform",
            runsOverride = 1,
            warmupOverride = 0,
            testFlow = ::executeFlow
        )

        reporter.generateReport(kmmMetrics, "kmm-verification")

        // Uncomment to verify Flutter as well
//        val flutterMetrics = runner.runTestSuite(
//            app = App.FLUTTER,
//            frameworkName = "Flutter",
//            runsOverride = 1,
//            warmupOverride = 0,
//            testFlow = ::executeFlow
//        )
//        reporter.generateReport(flutterMetrics, "flutter-verification")
    }

    @Test
    @DisplayName("Compare Frameworks Performance")
    @Tag("benchmark")
    fun compareFrameworks() {
        logger.info("🏁 Starting full benchmark comparison")
        
        // 1. Run KMM Benchmark
        val kmmMetrics = runner.runTestSuite(
            app = App.KMM,
            frameworkName = "Compose Multiplatform",
            testFlow = ::executeFlow
        )
        
        // Generate individual report
        reporter.generateReport(kmmMetrics, "kmm-benchmark")
        
        // 2. Run Flutter Benchmark
        val flutterMetrics = runner.runTestSuite(
            app = App.FLUTTER,
            frameworkName = "Flutter",
            testFlow = ::executeFlow
        )
        
        // Generate individual report
        reporter.generateReport(flutterMetrics, "flutter-benchmark")
        
        // 3. Compare and Report
        logger.info("\n🏆 Benchmark Complete")
        logger.info("==================================================")
        logger.info("Compose Multiplatform vs Flutter")
        logger.info("==================================================")
        
        val kmmActionMetrics = kmmMetrics.getActionMetrics()
        val flutterActionMetrics = flutterMetrics.getActionMetrics()
        
        logger.info("KMM Results:")
        logger.info("   App Launch Time: ${kmmMetrics.getAppLaunchTime()}ms")
        logger.info("   Actions collected: ${kmmActionMetrics.size}")
        kmmActionMetrics.forEach {
            logger.info("   - ${it.pageName}.${it.actionName}: ${it.durationMs}ms (CPU: ${String.format("%.1f", it.p50Cpu)}%, Mem: ${it.p50Memory.toInt()}MB)")
        }
        
        logger.info("Flutter Results:")
        logger.info("   App Launch Time: ${flutterMetrics.getAppLaunchTime()}ms")
        logger.info("   Actions collected: ${flutterActionMetrics.size}")
        flutterActionMetrics.forEach {
            logger.info("   - ${it.pageName}.${it.actionName}: ${it.durationMs}ms (CPU: ${String.format("%.1f", it.p50Cpu)}%, Mem: ${it.p50Memory.toInt()}MB)")
        }
    }
    
    /**
     * Execute the test flow (register user + create tasks).
     */
    private fun executeFlow(driver: WebDriver, app: App, metricsManager: MetricsManager) {
        // Initialize BaseScreen context for the new page objects
        BaseScreen.Context.init(driver, config.platform, app, metricsManager)
        
        try {
            // Register user
            val timestamp = System.currentTimeMillis()
            val name = "Test User"
            val email = "testuser_${timestamp}@test.com"
            val password = "TestPass123!"
            
             BaseScreen.on<LoginPage>()
                .clickRegisterLink()
                .register(name, email, password, password)
                 .on<TasksPage>()
                .doOnRepeat<TasksPage>(10) {
                    this.on<TasksPage>()
                        .clickAddTask()
                        .on<TaskCreatePage>()
                        .createTask("Task ${it + 1}")
                }
                .scrollDownToTask("Task 10")
                .scrollUpToTask("Task 1")
        } catch (e: Exception) {
            logger.error("Error during test flow execution", e)
            throw e
        } finally {
            // Clear context to avoid leaks or cross-test pollution
            BaseScreen.Context.clear()
        }
    }
}
