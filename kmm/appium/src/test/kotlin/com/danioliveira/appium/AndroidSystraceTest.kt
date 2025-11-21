package com.danioliveira.appium

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.metrics.LegacyMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.LoginPage
import com.danioliveira.appium.pages.TasksPage
import com.danioliveira.appium.reporting.MetricsReporter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Android-specific test demonstrating systrace capture integration.
 * 
 * This test captures:
 * - System-level performance via systrace
 * - Screen-level traces from SysTrace markers in the app
 * - CPU, Memory, FPS metrics via dumpsys
 * 
 * Requirements:
 * - Real Android device (API 29+ recommended for async traces)
 * - USB debugging enabled
 * - App built with SysTrace instrumentation
 * 
 * Run with:
 * ```
 * ./gradlew test --tests "AndroidSystraceTest" \
 *   -Dplatform=ANDROID \
 *   -DpackageName=com.danioliveira.taskmanager \
 *   -Dapk=path/to/app.apk
 * ```
 */
@EnabledIf("isAndroid")
class AndroidSystraceTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var metricsManager: MetricsManager
    private lateinit var metricsReporter: MetricsReporter
    
    companion object {
        @JvmStatic
        fun isAndroid(): Boolean {
            val platform = System.getenv("PLATFORM")?.uppercase() ?: "ANDROID"
            return platform == "ANDROID"
        }
    }
    
    @BeforeEach
    fun setup() {
        val platform = Platform.ANDROID
        val packageName = System.getProperty("packageName") ?: "com.danioliveira.taskmanager"
        
        // Create config
        val config = com.danioliveira.appium.config.BenchmarkConfig(
            platform = platform,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "systrace",
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
        
        // Initialize driver
        driver = AndroidDriverFactory.create(config)
        
        // Initialize metrics manager
        metricsManager = LegacyMetricsManager(platform, packageName, driver = driver)
        
        // Check systrace availability
        val apiLevel = metricsManager.let {
            (it as? MetricsManager)?.let { mgr ->
                mgr.javaClass.getDeclaredField("androidCollector").apply {
                    isAccessible = true
                }.get(mgr)?.let { collector ->
                    collector.javaClass.getMethod("systraceCollector").invoke(collector)?.let { systrace ->
                        systrace.javaClass.getMethod("getDeviceApiLevel").invoke(systrace) as? Int
                    }
                }
            }
        } ?: 0
        
        logger.info("Device API level: $apiLevel")
        if (apiLevel < 29) {
            logger.warn("⚠️  API level < 29. Async traces may not work properly.")
            logger.warn("   For best results, use Android 10+ (API 29+)")
        }
        
        // Initialize reporter
        val reportsDir = File("build/reports/systrace")
        metricsReporter = MetricsReporter(reportsDir)
        
        logger.info("✅ AndroidSystraceTest setup complete")
    }
    
    @Test
    fun testLoginFlowWithSystrace() {
        logger.info("=== Starting Login Flow with Systrace ===")
        
        // Start systrace capture
        metricsManager.startPerformanceRecording()
        
        // Give systrace a moment to initialize
        Thread.sleep(1000)
        
        // Measure app launch
        val launchTime = metricsManager.measureAppLaunchTime()
        logger.info("App launched in ${launchTime}ms")
        
        // Create page objects
        val loginPage = LoginPage(driver, Platform.ANDROID, metricsManager = metricsManager)
        
        // Execute login flow with metrics tracking
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("test@example.com")
        loginPage.enterPassword("password123")
        loginPage.clickLogin()
        
        // Wait for navigation
        Thread.sleep(2000)
        
        // Wait for systrace to complete
        logger.info("Waiting for systrace capture to complete...")
        Thread.sleep(27000) // Remaining time from 30s
        
        // Stop systrace and parse
        val traceMessage = metricsManager.stopPerformanceRecording("login_flow")
        logger.info("Systrace: $traceMessage")
        
        // Get trace metrics
        val traceMetrics = metricsManager.getTraceMetrics()
        if (traceMetrics != null) {
            logger.info("=== Systrace Results ===")
            logger.info("Trace file: ${traceMetrics.traceFile?.name}")
            logger.info("Total duration: ${traceMetrics.totalDurationMs}ms")
            logger.info("Screens captured: ${traceMetrics.screenCount}")
            
            traceMetrics.screens.forEach { screen ->
                logger.info("  ${screen.name}:")
                logger.info("    Start: ${screen.startTimeMs}ms")
                logger.info("    End: ${screen.endTimeMs}ms")
                logger.info("    Duration: ${screen.durationMs}ms")
            }
            
            // Check for expected screens
            val loginScreen = traceMetrics.getScreenByName("LoginScreen")
            val tasksScreen = traceMetrics.getScreenByName("TasksScreen")
            
            if (loginScreen != null) {
                logger.info("✅ LoginScreen trace found: ${loginScreen.durationMs}ms")
            } else {
                logger.warn("⚠️  LoginScreen trace not found")
            }
            
            if (tasksScreen != null) {
                logger.info("✅ TasksScreen trace found: ${tasksScreen.durationMs}ms")
            } else {
                logger.warn("⚠️  TasksScreen trace not found")
            }
        } else {
            logger.warn("⚠️  No trace metrics available")
        }
        
        // Get regular metrics
        val loginSummary = metricsManager.getPageSummary("Login")
        loginSummary?.let {
            logger.info("=== Login Page Metrics ===")
            logger.info("Actions: ${it.actionCount}")
            logger.info("Avg Duration: ${String.format("%.1f", it.avgDurationMs)}ms")
            logger.info("Avg Memory: ${String.format("%.1f", it.avgMemoryMb)}MB")
            logger.info("Avg CPU: ${String.format("%.1f", it.avgCpuPercent)}%")
            logger.info("Avg FPS: ${String.format("%.1f", it.avgFps)}")
        }
    }
    
    @Test
    fun testCompleteJourneyWithSystrace() {
        logger.info("=== Starting Complete Journey with Systrace ===")
        
        // Start systrace capture
        metricsManager.startPerformanceRecording()
        Thread.sleep(1000)
        
        // Measure app launch
        metricsManager.measureAppLaunchTime()
        
        // Login flow
        val loginPage = LoginPage(driver, Platform.ANDROID, App.KMM, metricsManager)
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("test@example.com")
        loginPage.enterPassword("password123")
        loginPage.clickLogin()
        
        Thread.sleep(2000)
        
        // Tasks flow (if you have TasksPage implemented)
        // val tasksPage = TasksPage(driver, Platform.ANDROID, metricsManager)
        // tasksPage.waitForTasksScreen()
        // tasksPage.clickCreateTask()
        // ... more interactions ...
        
        // Wait for systrace to complete
        logger.info("Waiting for systrace capture to complete...")
        Thread.sleep(55000)
        
        // Stop and parse
        val traceMessage = metricsManager.stopPerformanceRecording("complete_journey")
        logger.info("Systrace: $traceMessage")
        
        // Analyze results
        val traceMetrics = metricsManager.getTraceMetrics()
        traceMetrics?.let { metrics ->
            logger.info("=== Journey Trace Analysis ===")
            logger.info("Total screens: ${metrics.screenCount}")
            logger.info("Total duration: ${metrics.totalDurationMs}ms")
            logger.info("Avg screen duration: ${String.format("%.1f", metrics.avgScreenDurationMs)}ms")
            
            // Screen durations by name
            val durations = metrics.getScreenDurations()
            durations.forEach { (screenName, durationList) ->
                val avg = durationList.average()
                val min = durationList.minOrNull() ?: 0L
                val max = durationList.maxOrNull() ?: 0L
                logger.info("$screenName: avg=${String.format("%.1f", avg)}ms, min=${min}ms, max=${max}ms (${durationList.size} occurrences)")
            }
        }
    }
    
    @Test
    fun testSystraceAvailability() {
        logger.info("=== Testing Systrace Availability ===")
        
        // Access systrace collector through reflection (for testing)
        val androidCollector = metricsManager.javaClass.getDeclaredField("androidCollector").apply {
            isAccessible = true
        }.get(metricsManager)
        
        val systraceCollector = androidCollector?.javaClass?.getMethod("getSystraceCollector")?.invoke(androidCollector)
        
        if (systraceCollector != null) {
            val isAvailable = systraceCollector.javaClass.getMethod("isAvailable").invoke(systraceCollector) as Boolean
            val apiLevel = systraceCollector.javaClass.getMethod("getDeviceApiLevel").invoke(systraceCollector) as Int
            
            logger.info("Systrace available: $isAvailable")
            logger.info("Device API level: $apiLevel")
            
            if (isAvailable) {
                logger.info("✅ Systrace is available on this device")
                if (apiLevel >= 29) {
                    logger.info("✅ API level $apiLevel supports async traces")
                } else {
                    logger.warn("⚠️  API level $apiLevel - async traces may not work")
                }
            } else {
                logger.error("❌ Systrace not available on this device")
            }
        }
    }
    
    @AfterEach
    fun teardown() {
        try {
            // Generate reports
            logger.info("Generating performance reports...")
            metricsReporter.generateReport(metricsManager, "android-systrace-test")
            
            // Log total summary
            val totalSummary = metricsManager.getTotalSummary()
            logger.info("=== Total Performance Summary ===")
            logger.info("Platform: ${totalSummary.platform}")
            logger.info("App Launch: ${totalSummary.appLaunchTimeMs}ms")
            logger.info("Total Pages: ${totalSummary.pageCount}")
            logger.info("Total Actions: ${totalSummary.totalActions}")
            logger.info("Avg Memory: ${String.format("%.1f", totalSummary.avgMemoryMb)}MB")
            logger.info("Avg CPU: ${String.format("%.1f", totalSummary.avgCpuPercent)}%")
            logger.info("Avg FPS: ${String.format("%.1f", totalSummary.avgFps)}")
            
            // Log trace summary if available
            val traceMetrics = metricsManager.getTraceMetrics()
            if (traceMetrics != null) {
                logger.info("=== Systrace Summary ===")
                logger.info("Trace file: ${traceMetrics.traceFile?.absolutePath}")
                logger.info("Screens traced: ${traceMetrics.screenCount}")
                logger.info("Avg screen duration: ${String.format("%.1f", traceMetrics.avgScreenDurationMs)}ms")
            }
            
            logger.info("Reports saved to: build/reports/systrace/")
            
        } finally {
            driver.quit()
        }
    }
}

