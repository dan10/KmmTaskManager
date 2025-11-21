package com.danioliveira.appium

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.drivers.IOSDriverFactory
import com.danioliveira.appium.metrics.LegacyMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.LoginPage
import com.danioliveira.appium.reporting.MetricsReporter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Example test demonstrating comprehensive performance metrics collection.
 * 
 * This test tracks:
 * - App launch time
 * - UI rendering speed (FPS, frame time, jank)
 * - Memory consumption
 * - CPU usage
 * 
 * For every action on every page with per-page and total summaries.
 */
class PerformanceMetricsTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var platform: Platform
    private lateinit var metricsManager: MetricsManager
    private lateinit var metricsReporter: MetricsReporter
    
    @BeforeEach
    fun setup() {
        val platformEnv = System.getenv("PLATFORM")?.uppercase() ?: "ANDROID"
        platform = Platform.valueOf(platformEnv)
        
        // Initialize driver using config FIRST
        val udid = System.getProperty("udid")
        logger.info("=== DEBUG: UDID from system property: $udid ===")
        
        val config = com.danioliveira.appium.config.BenchmarkConfig(
            platform = platform,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "metrics",
            runs = 1,
            warmup = 0,
            deviceName = System.getProperty("deviceName") ?: when(platform) {
                Platform.IOS -> "iPhone 15 Pro"
                Platform.ANDROID -> "emulator-5554"
            },
            udid = udid,
            apkPath = System.getProperty("apk"),
            ipaPath = System.getProperty("ipa"),
            bundleId = System.getProperty("bundleId") ?: "com.danioliveira.taskmanager.KmmTaskManager",
            packageName = System.getProperty("packageName") ?: "com.danioliveira.taskmanager",
            registerEveryCycle = false
        )
        logger.info("=== DEBUG: Config UDID: ${config.udid} ===")
        
        driver = when (platform) {
            Platform.ANDROID -> AndroidDriverFactory.create(config)
            Platform.IOS -> IOSDriverFactory.create(config)
        }
        
        // Initialize metrics manager with driver
        metricsManager = when (platform) {
            Platform.ANDROID -> {
                val packageName = System.getProperty("packageName") 
                    ?: "com.danioliveira.taskmanager"
                logger.info("Using Android package: $packageName")
                LegacyMetricsManager(platform, packageName, driver = driver)
            }
            Platform.IOS -> {
                // Read from system properties (passed via -D flags)
                val bundleId = System.getProperty("bundleId") 
                    ?: System.getenv("IOS_BUNDLE_ID")
                    ?: "com.danioliveira.taskmanager.KmmTaskManager"
                val udid = System.getProperty("udid")
                    ?: System.getenv("IOS_UDID")
                
                // Get Instruments profile name
                val instrumentsProfile = System.getProperty("instrumentsProfile")
                    ?: System.getProperty("instruments.profile")
                    ?: System.getenv("INSTRUMENTS_PROFILE")
                    ?: "Allocations"  // Default to simulator-compatible profile
                
                logger.info("DEBUG: System property 'instrumentsProfile' = ${System.getProperty("instrumentsProfile")}")
                logger.info("DEBUG: Resolved profile = '$instrumentsProfile'")
                
                logger.info("Using iOS bundle ID: $bundleId, UDID: $udid")
                logger.info("Using Instruments profile: '$instrumentsProfile'")
                
                LegacyMetricsManager(
                    platform = platform,
                    packageOrBundleId = bundleId,
                    udid = udid,
                    driver = driver,
                    instrumentsProfileName = instrumentsProfile  // ✅ Pass custom profile!
                )
            }
        }
        
        // Initialize reporter
        val reportsDir = File("build/reports/metrics")
        metricsReporter = MetricsReporter(reportsDir)
        
        logger.info("Test setup complete for platform: $platform")
    }
    
    @Test
    fun testLoginFlowWithMetrics() {
        logger.info("Starting login flow performance test")
        
        // Check if Instruments recording is managed externally (e.g., by Gradle task)
        val skipRecording = System.getProperty("skipInstrumentsRecording")?.toBoolean() ?: false
        
        if (skipRecording) {
            logger.info("⚠️  Skipping internal Instruments recording (managed externally)")
            logger.info("📊 App was launched by Instruments - launch metrics will be in trace")
            // When recording is managed externally, the app is already running
            // and launch was captured by Instruments
        } else {
            // Start iOS performance recording (no-op for Android)
            metricsManager.startPerformanceRecording()
            
            // Measure app launch time
            val launchTime = metricsManager.measureAppLaunchTime()
            logger.info("App launched in ${launchTime}ms")
        }
        
        // Create page objects with metrics manager
        val loginPage = LoginPage(driver, platform, App.KMM, metricsManager)
        
        // All actions are automatically tracked
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("test@example.com")
        loginPage.enterPassword("password123")
        loginPage.clickLogin()
        
        // Wait a bit for navigation
        Thread.sleep(2000)
        
        if (!skipRecording) {
            // Wait for Instruments to finish writing trace data
            logger.info("Waiting for Instruments to finish writing...")
            Thread.sleep(3000)  // Give Instruments time to flush data
            
            // Stop iOS performance recording
            metricsManager.stopPerformanceRecording()?.let { message ->
                logger.info("iOS Performance Recording: $message")
            }
        }
        
        // Get and log page summary
        val loginPageSummary = metricsManager.getPageSummary("Login")
        loginPageSummary?.let {
            logger.info("Login Page Summary:")
            logger.info("  Actions: ${it.actionCount}")
            logger.info("  Avg Duration: ${String.format("%.1f", it.avgDurationMs)}ms")
            logger.info("  Avg Memory: ${String.format("%.1f", it.avgMemoryMb)}MB")
            logger.info("  Avg CPU: ${String.format("%.1f", it.avgCpuPercent)}%")
            logger.info("  Avg FPS: ${String.format("%.1f", it.avgFps)}")
        }
    }
    
    @Test
    fun testCompleteUserJourneyWithMetrics() {
        logger.info("Starting complete user journey performance test")
        
        // Start iOS performance recording
        metricsManager.startPerformanceRecording()
        
        // Measure app launch time
        metricsManager.measureAppLaunchTime()
        
        // Login flow
        val loginPage = LoginPage(driver, platform, App.KMM, metricsManager)
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("test@example.com")
        loginPage.enterPassword("password123")
        loginPage.clickLogin()
        
        Thread.sleep(2000)
        
        // Navigate to tasks (assuming you have navigation methods)
        // val navigationPage = NavigationPage(driver, platform, metricsManager)
        // navigationPage.goToTasks()
        
        // Interact with tasks page
        // val tasksPage = TasksPage(driver, platform, metricsManager)
        // tasksPage.addTask("New Task", "Description")
        // tasksPage.completeTask("New Task")
        
        // Stop iOS performance recording
        metricsManager.stopPerformanceRecording()
        
        logger.info("User journey complete")
    }
    
    @AfterEach
    fun teardown() {
        try {
            // Generate comprehensive reports
            logger.info("Generating performance metrics reports...")
            metricsReporter.generateReport(metricsManager, "performance-test")
            
            // Log total summary
            val totalSummary = metricsManager.getTotalSummary()
            logger.info("=== Total Performance Summary ===")
            logger.info("Platform: ${totalSummary.platform}")
            logger.info("App Launch Time: ${totalSummary.appLaunchTimeMs}ms")
            logger.info("Total Pages: ${totalSummary.pageCount}")
            logger.info("Total Actions: ${totalSummary.totalActions}")
            logger.info("Avg Memory: ${String.format("%.1f", totalSummary.avgMemoryMb)}MB " +
                "(Peak: ${totalSummary.peakMemoryMb}MB)")
            logger.info("Avg CPU: ${String.format("%.1f", totalSummary.avgCpuPercent)}% " +
                "(Peak: ${String.format("%.1f", totalSummary.peakCpuPercent)}%)")
            logger.info("Avg FPS: ${String.format("%.1f", totalSummary.avgFps)}")
            logger.info("Avg Frame Time: ${String.format("%.2f", totalSummary.avgFrameTimeMs)}ms")
            
            if (totalSummary.platform == "ANDROID") {
                logger.info("Avg Jank: ${String.format("%.1f", totalSummary.avgJankPercentage)}%")
            }
            
            logger.info("\nPer-Page Summaries:")
            totalSummary.pageSummaries.forEach { page ->
                logger.info("  ${page.pageName}: ${page.actionCount} actions, " +
                    "avg ${String.format("%.1f", page.avgDurationMs)}ms, " +
                    "${String.format("%.1f", page.avgMemoryMb)}MB, " +
                    "${String.format("%.1f", page.avgFps)} FPS")
            }
            
            logger.info("Reports saved to: build/reports/metrics/")
            
        } finally {
            driver.quit()
        }
    }
}

