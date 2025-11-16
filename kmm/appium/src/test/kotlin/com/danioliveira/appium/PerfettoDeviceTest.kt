package com.danioliveira.appium

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.pages.LoginPage
import com.danioliveira.appium.pages.TasksPage
import com.danioliveira.appium.perf.core.*
import com.danioliveira.appium.perf.measurePerformance
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Real device test to validate Perfetto integration.
 * 
 * This test:
 * 1. Uses Perfetto for trace capture (not Systrace)
 * 2. Captures a complete login flow
 * 3. Extracts jank stats, startup breakdown, and performance metrics
 * 4. Exports results in all formats (JSON, CSV, Markdown)
 * 
 * Prerequisites:
 * - trace_processor_shell installed (run: ./install-trace-processor.sh)
 * - Android device connected (check: adb devices)
 * - Appium server running (run: appium)
 * - App APK available
 */
class PerfettoDeviceTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private val packageName = System.getProperty("packageName") ?: "com.danioliveira.taskmanager"
    private var perfettoCollector: com.danioliveira.appium.metrics.android.PerfettoCollector? = null

    @BeforeEach
    fun setup() {
        // START PERFETTO BEFORE LAUNCHING APP
        // This ensures we capture all trace markers from app startup
        logger.info("🚀 Starting Perfetto BEFORE app launch...")
        perfettoCollector = com.danioliveira.appium.metrics.android.PerfettoCollector()
        
        val perfConfig = PerformanceConfig(
            platform = Platform.ANDROID,
            usePerfetto = true,
            systraceBufferSizeKb = 65536,  // Increased from 16384 (16MB) to 65536 (64MB)
            systraceCategories = listOf(
                "sched", "freq", "idle", "am", "wm", "gfx", "view",
                "binder_driver", "hal", "dalvik", "input", "res", "sync", "memory"
            )
        )
        
        val started = perfettoCollector!!.startCapture(perfConfig, packageName)
        if (!started) {
            logger.error("❌ Failed to start Perfetto")
            throw IllegalStateException("Could not start Perfetto capture")
        }
        logger.info("✅ Perfetto started, now launching app...")
        
        // Small delay to ensure Perfetto is ready
        Thread.sleep(500)
        
        // NOW launch the app - LoginScreen markers will be captured
        val config = BenchmarkConfig(
            platform = Platform.ANDROID,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "perfetto-device-test",
            runs = 1,
            warmup = 0,
            deviceName = System.getProperty("deviceName") ?: "197cc4507d7b",
            udid = System.getProperty("udid"),
            apkPath = System.getProperty("apk"),
            ipaPath = null,
            bundleId = null,
            packageName = packageName,
            registerEveryCycle = false
        )

        driver = AndroidDriverFactory.create(config)
        logger.info("✅ App launched, PerfettoDeviceTest setup complete")
    }

    @Test
    fun testLoginWithPerfetto() = runBlocking {
        // 2 minute timeout using Kotlin coroutines
        withTimeout(120_000) {
            logger.info("=== Starting Perfetto Device Test ===")
            logger.info("📱 Device: ${System.getProperty("deviceName") ?: "197cc4507d7b"}")
            logger.info("📦 Package: $packageName")
            logger.info("🔧 Using Perfetto for trace capture")
            logger.info("⏱️  Timeout: 2 minutes")

        val loginPage = LoginPage(driver, Platform.ANDROID)
        val tasksPage = TasksPage(driver, Platform.ANDROID)

        // Define the test case
        val testCase = TestCase(
            beforeTest = {
                logger.info("🔄 Preparing test - Perfetto has already captured LoginScreen from app launch...")
                // No need to restart - Perfetto was started BEFORE app launch in @BeforeEach
                // LoginScreen is already being traced from the initial app launch
                delay(500) // Small delay to ensure LoginScreen is fully rendered
            },
            run = {
                logger.info("📝 Waiting for LoginScreen to be fully rendered...")
                loginPage.waitForLoginScreen()
                // Give LoginScreen MORE time to be traced after Perfetto starts
                // This ensures TraceLifecycle("LoginScreen") has time to emit the marker
                delay(2000)
                logger.info("✅ LoginScreen should now be traced in Perfetto")
                
                logger.info("📝 Entering credentials...")
                
                logger.info("📧 Entering email...")
                loginPage.enterEmail("daniel@test.com")
                delay(500)
                
                logger.info("🔒 Entering password...")
                loginPage.enterPassword("12345678")
                delay(500)
                
                logger.info("🔐 Clicking login...")
                loginPage.clickLogin()
                // Give more time for navigation and TasksScreen to mount
                delay(3000)
                
                logger.info("✅ Waiting for tasks screen...")
                tasksPage.waitForTasksList()
                // Give TasksScreen time to be traced
                delay(2000)
                logger.info("✅ TasksScreen should now be traced in Perfetto")
                
                logger.info("🧹 Test complete")
            }
        )

        // Configure performance measurement with Perfetto
        val perfConfig = PerformanceConfig(
            platform = Platform.ANDROID,
            usePerfetto = true,  // ✅ Use Perfetto instead of Systrace
            enableCpuProfiling = true,
            enableMemoryProfiling = true,
            enableFpsProfiling = true,
            pollingIntervalMs = 500,
            systraceBufferSizeKb = 16384,  // 16MB buffer (optimized for short tests)
            systraceCategories = listOf(
                "sched", "freq", "idle", "am", "wm", "gfx", "view",
                "binder_driver", "hal", "dalvik", "input", "res", "sync", "memory"
            )
        )

        logger.info("🚀 Running test with Perfetto already capturing...")
        
        // Run the test WITHOUT starting a new Perfetto session
        // (Perfetto is already running from @BeforeEach)
        val testStartTime = System.currentTimeMillis()
        
        try {
            testCase.beforeTest()
            delay(500)
            testCase.run()
            delay(2000)
        } catch (e: Exception) {
            logger.error("Test failed: ${e.message}", e)
            throw e
        }
        
        val testEndTime = System.currentTimeMillis()
        val testDuration = testEndTime - testStartTime
        
        // Stop Perfetto and parse trace
        logger.info("🛑 Stopping Perfetto capture...")
        val traceFile = perfettoCollector!!.stopCapture("performance_test")
        
        if (traceFile == null) {
            logger.error("❌ Failed to capture trace")
            throw IllegalStateException("Perfetto trace file is null")
        }
        
        logger.info("✅ Trace captured: ${traceFile.name} (${traceFile.length() / 1024 / 1024}MB)")
        
        // Parse the trace
        logger.info("📊 Parsing trace metrics...")
        val traceMetrics = perfettoCollector!!.parseTrace(traceFile)
        
        // Build result manually since we're not using measurePerformance()
        // Use MetricStats.from(...) so all statistics (min/max/avg/p90/p95/p99/stddev) come from the
        // same Apache Commons Math pipeline used elsewhere.
        val cpuSamples = traceMetrics.cpuUtilization.map { it.cpuPercent }
        val cpuStats = if (cpuSamples.isNotEmpty()) {
            MetricStats.from(cpuSamples)
        } else {
            MetricStats.empty()
        }

        // We currently don't expose a full memory time series from PerfettoCollector here,
        // so keep memory metrics empty for now.
        val memoryStats = MetricStats.empty()

        // FPS: we have an average fps and frameCount – approximate distribution by treating
        // every frame as having the same fps value.
        val fpsValues = if (traceMetrics.frameCount > 0) {
            List(traceMetrics.frameCount) { traceMetrics.fps }
        } else {
            emptyList()
        }
        val fpsStats = if (fpsValues.isNotEmpty()) MetricStats.from(fpsValues) else MetricStats.empty()

        val result = PerformanceResult(
            testName = "performance_test",
            durationMs = testDuration,
            metrics = PerformanceMetrics(
                cpu = cpuStats,
                memory = memoryStats,
                fps = fpsStats
            ),
            traceFile = traceFile,
            screenMetrics = traceMetrics.screenMetrics,
            fpsPerSecond = traceMetrics.fpsPerSecond
        )

        // Export results
        val outputDir = File("build/reports/perfetto-device-test")
        result.writeAll(outputDir)

        // Log summary
        logger.info("")
        logger.info("=" .repeat(70))
        logger.info("📊 PERFETTO DEVICE TEST RESULTS")
        logger.info("=" .repeat(70))
        logger.info("")
        logger.info("⏱️  Duration: ${result.durationMs}ms")
        logger.info("📁 Trace: ${result.traceFile?.name ?: "N/A"}")
        logger.info("")
        
        // Metrics
        logger.info("📈 Performance Metrics:")
        logger.info("   CPU:    ${String.format("%.1f", result.metrics.cpu.avg)}% avg, ${String.format("%.1f", result.metrics.cpu.max)}% max")
        logger.info("   Memory: ${result.metrics.memory.avg}MB avg, ${result.metrics.memory.max}MB max")
        logger.info("   FPS:    ${String.format("%.1f", result.metrics.fps.avg)} avg, ${String.format("%.1f", result.metrics.fps.max)} max")
        logger.info("")
        
        // Jank stats
        if (result.jankStats != null) {
            logger.info("🎬 Jank Analysis:")
            logger.info("   Total Frames: ${result.jankStats.totalFrames}")
            logger.info("   Jank Frames:  ${result.jankStats.jankFrames} (${String.format("%.1f", result.jankStats.jankPercentage)}%)")
            logger.info("   Avg Frame:    ${String.format("%.2f", result.jankStats.avgFrameTimeMs)}ms")
            logger.info("   P50:          ${String.format("%.2f", result.jankStats.p50FrameTimeMs)}ms")
            logger.info("   P90:          ${String.format("%.2f", result.jankStats.p90FrameTimeMs)}ms")
            logger.info("   P99:          ${String.format("%.2f", result.jankStats.p99FrameTimeMs)}ms")
            logger.info("")
        }
        
        // Startup breakdown
        if (result.startupBreakdown != null) {
            logger.info("🚀 App Startup:")
            logger.info("   Type:     ${result.startupBreakdown.type}")
            logger.info("   Duration: ${String.format("%.0f", result.startupBreakdown.totalMs)}ms")
            if (result.startupBreakdown.phases.isNotEmpty()) {
                logger.info("   Phases:")
                result.startupBreakdown.phases.forEach { phase ->
                    logger.info("      • ${phase.name}: ${String.format("%.0f", phase.durationMs)}ms (${String.format("%.1f", phase.percentageOfTotal)}%)")
                }
            }
            logger.info("")
        }
        
        logger.info("📂 Results exported to: ${outputDir.absolutePath}")
        logger.info("   • performance_result.json")
        logger.info("   • performance_result.md")
        logger.info("")
        logger.info("=" .repeat(70))
        logger.info("✅ Perfetto Device Test Complete!")
        logger.info("=" .repeat(70))
        
            // Close the app using ADB
            try {
                val adbPath = System.getProperty("user.home") + "/Library/Android/sdk/platform-tools/adb"
                Runtime.getRuntime().exec(arrayOf(adbPath, "shell", "am", "force-stop", packageName)).waitFor()
                logger.info("✅ App closed")
            } catch (e: Exception) {
                logger.warn("Failed to close app: ${e.message}")
            }
        } // end withTimeout
    }

    @AfterEach
    fun teardown() {
        // Ensure Perfetto is stopped if test failed
        perfettoCollector?.let {
            try {
                it.stopCapture("cleanup")
                logger.info("✅ Perfetto stopped in teardown")
            } catch (e: Exception) {
                logger.warn("Failed to stop Perfetto in teardown: ${e.message}")
            }
        }
        
        driver.quit()
        logger.info("✅ Driver quit")
    }
}

