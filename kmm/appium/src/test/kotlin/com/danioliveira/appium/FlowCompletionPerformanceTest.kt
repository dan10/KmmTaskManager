package com.danioliveira.appium

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.flows.Flows
import com.danioliveira.appium.perf.core.*
import com.danioliveira.appium.perf.measurePerformance
import io.appium.java_client.android.AndroidDriver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Full flow completion performance test using Flows DSL.
 * 
 * Flow:
 * 1. Register User (creates unique user per iteration)
 * 2. Create tasks using Flows DSL
 * 3. Scroll to verify all tasks visible
 * 
 * Each iteration clears app data and registers a new user to ensure clean state.
 * Aggregates all samples across runs for combined metrics with histograms.
 */
class FlowCompletionPerformanceTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: AndroidDriver
    private lateinit var androidCollector: AndroidMetricsCollector
    private lateinit var flows: Flows
    private val packageName = "com.danioliveira.taskmanager"
    
    @BeforeEach
    fun setup() {
        val config = BenchmarkConfig(
            platform = Platform.ANDROID,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "flow-completion",
            runs = 1,
            warmup = 0,
            deviceName = /*System.getProperty("deviceName") ?:*/ "emulator-5554",
            udid = System.getProperty("udid"),
            apkPath = System.getProperty("apk"),
            ipaPath = null,
            bundleId = null,
            packageName = packageName,
            registerEveryCycle = false
        )
        
        driver = AndroidDriverFactory.create(config)
        androidCollector = AndroidMetricsCollector(packageName)
        flows = Flows(driver, Platform.ANDROID)
        
        logger.info("✅ FlowCompletionPerformanceTest setup complete")
    }
    
    @Test
    fun testFullFlowCompletion() = runBlocking {
        logger.info("=== Full Flow Completion Performance Test ===")
        logger.info("Executing 1 iteration of register + flow")
        
        val results = mutableListOf<PerformanceResult>()
        val iterations = 1
        
        for (iteration in 1..iterations) {
            logger.info("")
            logger.info("=" .repeat(70))
            logger.info("ITERATION $iteration/$iterations")
            logger.info("=" .repeat(70))
            
            try {
                val result = executeFlowIteration(iteration)
                results.add(result)
                
                // Export individual run
                val runDir = File("build/reports/flow-completion/run_$iteration")
                result.writeAll(runDir)
                logger.info("✅ Iteration $iteration complete - results saved to ${runDir.absolutePath}")
                
            } catch (e: Exception) {
                logger.error("❌ Iteration $iteration failed: ${e.message}", e)
            }
        }
        
        // For a single run we still aggregate, but effectively it's just that run.
        logger.info("")
        logger.info("=" .repeat(70))
        logger.info("ALL ITERATIONS COMPLETE - AGGREGATING RESULTS")
        logger.info("=" .repeat(70))
        
        aggregateAndExportResults(results)
    }
    
    private suspend fun executeFlowIteration(iteration: Int): PerformanceResult {
        val testCase = TestCase(
            beforeTest = {
                logger.info("🔄 Preparing iteration $iteration...")
                resetAppState()
            },
            run = {
                logger.info("🚀 Executing flow...")
                
                // 1. Register User and create initial task using Flows DSL
                val timestamp = System.currentTimeMillis()
                val name = "Test User $iteration"
                val email = "testuser${iteration}_${timestamp}@test.com"
                val password = "TestPass123!"
                
                val tasksPage = flows.registerUser(name, email, password)
                delay(500)
                
                // 2. Create bulk tasks using Flows DSL
                val taskTitles = (1..10).map { "Task $it - Performance Test" }
                val finalTasksPage = flows.createBulkTasks(taskTitles)
                
                // 3. Scroll to verify all tasks visible
                finalTasksPage.scrollToEnd()
                delay(1000)
                
                logger.info("✅ Flow complete")
            },
            durationMs = 5000 // Wait time after flow completes
        )
        
        val config = PerformanceConfig(
            platform = Platform.ANDROID,
            usePerfetto = true,
            enableCpuProfiling = true,
            enableMemoryProfiling = true,
            enableFpsProfiling = true,
            pollingIntervalMs = 500,
            systraceBufferSizeKb = 65536
        )
        
        return measurePerformance(
            packageName = packageName,
            testCase = testCase,
            config = config,
            androidCollector = androidCollector
        )
    }
    
    // Helper methods for flow steps
    
    private suspend fun resetAppState() {
        try {
            val adbPath = System.getProperty("user.home") + "/Library/Android/sdk/platform-tools/adb"
            
            // Force stop app
            Runtime.getRuntime().exec(arrayOf(adbPath, "shell", "am", "force-stop", packageName)).waitFor()
            delay(1000)
            
            // Clear app data so we can register new users each iteration
            Runtime.getRuntime().exec(arrayOf(adbPath, "shell", "pm", "clear", packageName)).waitFor()
            delay(2000)
            
            // Restart the app
            Runtime.getRuntime().exec(arrayOf(
                adbPath, "shell", "am", "start", "-n", "$packageName/.MainActivity"
            )).waitFor()
            delay(3000)
            
            logger.info("✅ App state reset (data cleared)")
        } catch (e: Exception) {
            logger.warn("Failed to reset app state: ${e.message}")
        }
    }
    
    
    private fun aggregateAndExportResults(results: List<PerformanceResult>) {
        if (results.isEmpty()) {
            logger.error("❌ No results to aggregate")
            return
        }
        
        logger.info("📊 Aggregating ${results.size} results...")
        
        // Aggregate with histograms
        val aggregated = AggregatedResultWithHistograms.from(results, packageName)
        
        // Export aggregated results
        val outputDir = File("build/reports/flow-completion")
        outputDir.mkdirs()
        
        aggregated.writeAll(outputDir)
        
        // Log summary
        logger.info("")
        logger.info("=" .repeat(70))
        logger.info("📊 AGGREGATED RESULTS SUMMARY")
        logger.info("=" .repeat(70))
        logger.info("")
        logger.info("Total Runs: ${aggregated.totalRuns}")
        logger.info("Successful Runs: ${aggregated.successfulRuns}")
        logger.info("")
        logger.info("Combined Statistics (from merged samples):")
        logger.info("  CPU:      min=${String.format("%.1f", aggregated.cpuStats.min)}%, " +
            "max=${String.format("%.1f", aggregated.cpuStats.max)}%, " +
            "avg=${String.format("%.1f", aggregated.cpuStats.avg)}%, " +
            "p90=${String.format("%.1f", aggregated.cpuStats.p90)}%, " +
            "p95=${String.format("%.1f", aggregated.cpuStats.p95)}%, " +
            "p99=${String.format("%.1f", aggregated.cpuStats.p99)}% " +
            "(${aggregated.cpuStats.samples} samples)")
        logger.info("  Memory:   min=${String.format("%.1f", aggregated.memoryStats.min)}MB, " +
            "max=${String.format("%.1f", aggregated.memoryStats.max)}MB, " +
            "avg=${String.format("%.1f", aggregated.memoryStats.avg)}MB, " +
            "p90=${String.format("%.1f", aggregated.memoryStats.p90)}MB, " +
            "p95=${String.format("%.1f", aggregated.memoryStats.p95)}MB, " +
            "p99=${String.format("%.1f", aggregated.memoryStats.p99)}MB " +
            "(${aggregated.memoryStats.samples} samples)")
        logger.info("  FPS:      min=${String.format("%.1f", aggregated.fpsStats.min)}, " +
            "max=${String.format("%.1f", aggregated.fpsStats.max)}, " +
            "avg=${String.format("%.1f", aggregated.fpsStats.avg)}, " +
            "p90=${String.format("%.1f", aggregated.fpsStats.p90)}, " +
            "p95=${String.format("%.1f", aggregated.fpsStats.p95)}, " +
            "p99=${String.format("%.1f", aggregated.fpsStats.p99)} " +
            "(${aggregated.fpsStats.samples} samples)")
        logger.info("  Duration: min=${String.format("%.0f", aggregated.durationStats.min)}ms, " +
            "max=${String.format("%.0f", aggregated.durationStats.max)}ms, " +
            "avg=${String.format("%.0f", aggregated.durationStats.avg)}ms, " +
            "p90=${String.format("%.0f", aggregated.durationStats.p90)}ms, " +
            "p95=${String.format("%.0f", aggregated.durationStats.p95)}ms, " +
            "p99=${String.format("%.0f", aggregated.durationStats.p99)}ms " +
            "(${aggregated.durationStats.samples} samples)")
        logger.info("")
        logger.info("Histograms:")
        logger.info("")
        logger.info("CPU Distribution:")
        logger.info(aggregated.cpuHistogram.toAscii())
        logger.info("")
        logger.info("Memory Distribution:")
        logger.info(aggregated.memoryHistogram.toAscii())
        logger.info("")
        logger.info("FPS Distribution:")
        logger.info(aggregated.fpsHistogram.toAscii())
        logger.info("")
        logger.info("=" .repeat(70))
        logger.info("✅ Aggregation complete - results saved to ${outputDir.absolutePath}")
        logger.info("   • aggregated_results.json")
        logger.info("   • aggregated_results.md")
        logger.info("   • run_summaries.csv")
        logger.info("=" .repeat(70))
    }
    
    @AfterEach
    fun teardown() {
        driver.quit()
        logger.info("✅ Test teardown complete")
    }
}

