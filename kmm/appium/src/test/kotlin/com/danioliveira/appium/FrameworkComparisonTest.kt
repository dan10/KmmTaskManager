package com.danioliveira.appium

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.drivers.IOSDriverFactory
import com.danioliveira.appium.metrics.ImprovedMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.pages.PageFactory
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
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
    private val runs = config.runs
    private val warmup = config.warmup
    
    private val resultsDir = File("build/comparison-results").apply { mkdirs() }
    
    @Test
    @DisplayName("Compare CMP vs Flutter Performance")
    fun compareFrameworks() {
        logger.info("🚀 Starting Framework Comparison Test")
        logger.info("   Platform: ${config.platform}")
        logger.info("   Runs: $runs (warmup: $warmup)")
        
        val cmpResults = runTestSuite(App.KMM, "CMP")
        val flutterResults = runTestSuite(App.FLUTTER, "Flutter")
        
        // Generate comparison report
        generateComparisonReport(cmpResults, flutterResults)
        
        logger.info("✅ Framework comparison complete")
    }

    @Test
    @DisplayName("Run Single Iteration (Verification)")
    fun testSingleIteration() {
        logger.info("🚀 Starting Single Iteration Verification")
        logger.info("   Platform: ${config.platform}")
        
        // Override config for single run
        val singleRunConfig = config.copy(runs = 1, warmup = 0)
        
        // We can't easily inject the config into runTestSuite without changing its signature or the class structure.
        // Instead, we'll just call runTestSuite with the current config but only for one app to verify it works.
        // Ideally, we should refactor to pass config to runTestSuite.
        
        // For now, let's just run the KMM suite as a smoke test.
        val cmpResults = runTestSuite(App.KMM, "CMP", runsOverride = 1, warmupOverride = 0)
        
        logger.info("✅ Single iteration complete")
        logger.info("   App Launch Time: ${cmpResults.appLaunchTimeMs}ms")
        logger.info("   Actions collected: ${cmpResults.actionMetrics.size}")
        
        cmpResults.actionMetrics.forEach {
            logger.info("   - ${it.pageName}.${it.actionName}: ${it.durationMs}ms (CPU: ${String.format("%.1f", it.p50Cpu)}%, Mem: ${it.p50Memory.toInt()}MB)")
        }
    }
    
    /**
     * Run the test suite for a specific framework.
     */
    private fun runTestSuite(
        app: App, 
        frameworkName: String, 
        runsOverride: Int? = null, 
        warmupOverride: Int? = null
    ): TestSuiteResults {
        val currentRuns = runsOverride ?: runs
        val currentWarmup = warmupOverride ?: warmup
        
        logger.info("\n📊 Running test suite for $frameworkName")
        
        val driver = createDriver(app)
        val packageId = when (app) {
            App.KMM -> config.packageName ?: "com.danioliveira.taskmanager"
            App.FLUTTER -> config.packageName ?: "com.danioliveira.taskmanager_flutter"
        }
        
        val androidCollector = if (config.platform == Platform.ANDROID) {
             AndroidMetricsCollector(packageId)
        } else null

        val metricsManager = ImprovedMetricsManager(
            platform = config.platform,
            packageOrBundleId = packageId,
            androidCollector = androidCollector
        )
        
        try {
            // Start performance recording
            metricsManager.startPerformanceRecording()
            
            // Warmup runs (discarded)
            if (currentWarmup > 0) {
                logger.info("🔥 Running $currentWarmup warmup iterations (data will be discarded)...")
                repeat(currentWarmup) { iteration ->
                    logger.info("   Warmup iteration ${iteration + 1}/$currentWarmup")
                    executeFlow(driver, app, metricsManager)
                    Thread.sleep(2000) // Brief pause between iterations
                }
                metricsManager.reset() // Clear warmup data
                logger.info("✅ Warmup complete, starting measured runs")
            }
            
            // Measured runs
            // Run test iterations
            for (i in 1..currentRuns) {
                logger.info("Running iteration $i/$currentRuns...")
                
                // Reset metrics for this iteration
                metricsManager.reset()
                
                // Execute the test flow
                executeFlow(driver, app, metricsManager)
                
                Thread.sleep(200) // Brief pause between iterations
            }
            
            // Stop performance recording and parse trace
            metricsManager.stopPerformanceRecording()
            
            // NOW get the action metrics (after trace has been parsed and metrics updated)
            val allActionMetrics = metricsManager.getActionMetrics()
            
            // Analyze trace segments
            val traceSegments = metricsManager.analyzeTraceSegments()
            
            return TestSuiteResults(
                framework = frameworkName,
                app = app,
                iterations = currentRuns,
                actionMetrics = allActionMetrics,
                traceSegments = traceSegments,
                appLaunchTimeMs = metricsManager.getAppLaunchTime()
            )
            
        } finally {
            driver.quit()
        }
    }
    
    /**
     * Execute the test flow (register user + create tasks).
     */
    private fun executeFlow(driver: WebDriver, app: App, metricsManager: MetricsManager) {
        val loginPage = PageFactory.createLoginPage(driver, config.platform, app, metricsManager)
        
        // Register user
        val timestamp = System.currentTimeMillis()
        val name = "Test User"
        val email = "testuser_${timestamp}@test.com"
        val password = "TestPass123!"
        
        val tasksPage = loginPage
            .clickRegisterLink()
            .register(name, email, password)
        
        // Wait for tasks list to be ready
        tasksPage.waitForTasksList()
        Thread.sleep(500)
        
        // Create bulk tasks
        val taskTitles = (1..10).map { "Task $it - Performance Test" }
        var currentTasksPage: AbstractTasksPage = tasksPage
        
        for (title in taskTitles) {
            val createPage = currentTasksPage.openCreateTask()
            Thread.sleep(800) // Give bottom sheet time to appear
            currentTasksPage = createPage.quickCreateByInstance(title)
            Thread.sleep(500) // Brief delay between creations
        }
        
        // Scroll to verify all tasks visible
        currentTasksPage.scrollToTask("Task 10 - Performance Test")
        Thread.sleep(1000)
    }
    
    /**
     * Create driver for the specified app.
     */
    private fun createDriver(app: App): WebDriver {
        val appConfig = config.copy(app = app)
        return when (config.platform) {
            Platform.ANDROID -> {
                AndroidDriverFactory.create(appConfig)
            }
            Platform.IOS -> {
                IOSDriverFactory.create(appConfig)
            }
        }
    }
    
    /**
     * Generate comparison report with aggregated statistics.
     */
    private fun generateComparisonReport(cmpResults: TestSuiteResults, flutterResults: TestSuiteResults) {
        logger.info("\n📈 Generating comparison report...")
        
        // Aggregate metrics by action
        val cmpAggregated = aggregateMetrics(cmpResults.actionMetrics)
        val flutterAggregated = aggregateMetrics(flutterResults.actionMetrics)
        
        // Generate JSON reports
        val cmpReportFile = File(resultsDir, "report_cmp.json")
        val flutterReportFile = File(resultsDir, "report_flutter.json")
        
        gson.toJson(cmpResults, cmpReportFile.writer())
        gson.toJson(flutterResults, flutterReportFile.writer())
        
        logger.info("✅ JSON reports saved:")
        logger.info("   ${cmpReportFile.absolutePath}")
        logger.info("   ${flutterReportFile.absolutePath}")
        
        // Generate markdown comparison
        val comparisonMarkdown = buildComparisonMarkdown(cmpAggregated, flutterAggregated, cmpResults, flutterResults)
        val markdownFile = File(resultsDir, "comparison_report.md")
        markdownFile.writeText(comparisonMarkdown)
        
        logger.info("✅ Comparison report saved: ${markdownFile.absolutePath}")
    }
    
    /**
     * Aggregate metrics by action, calculating percentiles.
     */
    private fun aggregateMetrics(metrics: List<com.danioliveira.appium.metrics.ActionMetrics>): Map<String, AggregatedActionMetrics> {
        val grouped = metrics.groupBy { "${it.pageName}.${it.actionName}" }
        
        return grouped.mapValues { (actionKey, actionMetrics) ->
            val durations = actionMetrics.map { it.durationMs }.sorted()
            val memoryValues = actionMetrics.map { it.memoryMb }.sorted()
            val cpuValues = actionMetrics.map { it.cpuPercent }.sorted()
            val fpsValues = actionMetrics.map { it.fps }.sorted()
            
            AggregatedActionMetrics(
                actionKey = actionKey,
                count = actionMetrics.size,
                durationP50 = percentile(durations, 50),
                durationP90 = percentile(durations, 90),
                durationP95 = percentile(durations, 95),
                durationP99 = percentile(durations, 99),
                memoryP50 = percentile(memoryValues, 50),
                memoryP90 = percentile(memoryValues, 90),
                memoryP95 = percentile(memoryValues, 95),
                memoryP99 = percentile(memoryValues, 99),
                cpuP50 = percentile(cpuValues, 50),
                cpuP90 = percentile(cpuValues, 90),
                cpuP95 = percentile(cpuValues, 95),
                cpuP99 = percentile(cpuValues, 99),
                fpsP50 = percentile(fpsValues, 50),
                fpsP90 = percentile(fpsValues, 90),
                fpsP95 = percentile(fpsValues, 95),
                fpsP99 = percentile(fpsValues, 99)
            )
        }
    }
    
    /**
     * Calculate percentile from sorted list.
     */
    private fun <T : Number> percentile(sorted: List<T>, p: Int): Double {
        if (sorted.isEmpty()) return 0.0
        val index = (p / 100.0 * (sorted.size - 1)).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index].toDouble()
    }
    
    /**
     * Build markdown comparison report.
     */
    private fun buildComparisonMarkdown(
        cmpAggregated: Map<String, AggregatedActionMetrics>,
        flutterAggregated: Map<String, AggregatedActionMetrics>,
        cmpResults: TestSuiteResults,
        flutterResults: TestSuiteResults
    ): String {
        val sb = StringBuilder()
        
        sb.appendLine("# Framework Performance Comparison")
        sb.appendLine()
        sb.appendLine("**Platform:** ${config.platform}")
        sb.appendLine("**Iterations:** $runs (warmup: $warmup)")
        sb.appendLine()
        
        // Summary
        sb.appendLine("## Summary")
        sb.appendLine()
        sb.appendLine("| Metric | CMP | Flutter | Winner |")
        sb.appendLine("|-------|-----|---------|--------|")
        sb.appendLine("| App Launch Time | ${cmpResults.appLaunchTimeMs}ms | ${flutterResults.appLaunchTimeMs}ms | ${if (cmpResults.appLaunchTimeMs < flutterResults.appLaunchTimeMs) "CMP" else "Flutter"} |")
        sb.appendLine()
        
        // Per-action comparison
        sb.appendLine("## Per-Action Metrics")
        sb.appendLine()
        
        val allActions = (cmpAggregated.keys + flutterAggregated.keys).distinct().sorted()
        
        for (action in allActions) {
            val cmp = cmpAggregated[action]
            val flutter = flutterAggregated[action]
            
            sb.appendLine("### $action")
            sb.appendLine()
            sb.appendLine("| Percentile | CMP Duration (ms) | Flutter Duration (ms) | Winner |")
            sb.appendLine("|-----------|------------------|----------------------|--------|")
            
            if (cmp != null && flutter != null) {
                sb.appendLine("| P50 | ${cmp.durationP50.toInt()} | ${flutter.durationP50.toInt()} | ${if (cmp.durationP50 < flutter.durationP50) "CMP" else "Flutter"} |")
                sb.appendLine("| P90 | ${cmp.durationP90.toInt()} | ${flutter.durationP90.toInt()} | ${if (cmp.durationP90 < flutter.durationP90) "CMP" else "Flutter"} |")
                sb.appendLine("| P95 | ${cmp.durationP95.toInt()} | ${flutter.durationP95.toInt()} | ${if (cmp.durationP95 < flutter.durationP95) "CMP" else "Flutter"} |")
                sb.appendLine("| P99 | ${cmp.durationP99.toInt()} | ${flutter.durationP99.toInt()} | ${if (cmp.durationP99 < flutter.durationP99) "CMP" else "Flutter"} |")
            } else {
                sb.appendLine("| - | ${cmp?.durationP50?.toInt() ?: "N/A"} | ${flutter?.durationP50?.toInt() ?: "N/A"} | - |")
            }
            
            sb.appendLine()
        }
        
        return sb.toString()
    }
}

/**
 * Results from a test suite run.
 */
data class TestSuiteResults(
    val framework: String,
    val app: App,
    val iterations: Int,
    val actionMetrics: List<com.danioliveira.appium.metrics.ActionMetrics>,
    val traceSegments: Map<String, com.danioliveira.appium.metrics.TraceSegmentMetrics>,
    val appLaunchTimeMs: Long
)

/**
 * Aggregated metrics for an action across multiple iterations.
 */
data class AggregatedActionMetrics(
    val actionKey: String,
    val count: Int,
    val durationP50: Double,
    val durationP90: Double,
    val durationP95: Double,
    val durationP99: Double,
    val memoryP50: Double,
    val memoryP90: Double,
    val memoryP95: Double,
    val memoryP99: Double,
    val cpuP50: Double,
    val cpuP90: Double,
    val cpuP95: Double,
    val cpuP99: Double,
    val fpsP50: Double,
    val fpsP90: Double,
    val fpsP95: Double,
    val fpsP99: Double
)

