package com.danioliveira.appium.runner

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.drivers.IOSDriverFactory
import com.danioliveira.appium.metrics.ImprovedMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory

class BenchmarkRunner(
    private val config: BenchmarkConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun runTestSuite(
        app: App,
        frameworkName: String,
        runsOverride: Int? = null,
        warmupOverride: Int? = null,
        platformOverride: Platform? = null,
        testFlow: (WebDriver, App, MetricsManager, Platform) -> Unit
    ): MetricsManager {
        val currentRuns = runsOverride ?: config.runs
        val currentWarmup = warmupOverride ?: config.warmup
        val currentPlatform = platformOverride ?: config.platform

        logger.info("\n📊 Running test suite for $frameworkName on $currentPlatform")
        var driver: WebDriver? = null

        // Determine package ID before creating driver
        val packageId = when (app) {
            App.KMM -> config.packageName ?: "com.danioliveira.taskmanager"
            App.FLUTTER -> config.packageName ?: if (currentPlatform == Platform.ANDROID)
                "com.example.task_manager_app"
            else "com.danioliveira.taskManagerApp"
        }

        // Initialize metrics collector BEFORE creating driver
        val androidCollector = if (currentPlatform == Platform.ANDROID) {
            AndroidMetricsCollector(packageId)
        } else null

        val metricsManager = ImprovedMetricsManager(
            platform = currentPlatform,
            packageOrBundleId = packageId,
            androidCollector = androidCollector
        )

        try {
            // CRITICAL: Start performance recording BEFORE creating driver
            // This ensures app launch events are captured in the Perfetto trace
            metricsManager.startPerformanceRecording()

            // NOW create driver - app launch will be captured!
            driver = createDriver(app, currentPlatform)

            // Warmup runs (discarded)
            if (currentWarmup > 0) {
                logger.info("🔥 Running $currentWarmup warmup iterations (data will be discarded)...")
                repeat(currentWarmup) { iteration ->
                    logger.info("   Warmup iteration ${iteration + 1}/$currentWarmup")
                    testFlow(driver, app, metricsManager, currentPlatform)
                    Thread.sleep(2000) // Brief pause between iterations
                }
                metricsManager.reset() // Clear warmup data
                logger.info("✅ Warmup complete, starting measured runs")
            }

            // Measured runs
            for (i in 1..currentRuns) {
                logger.info("Running iteration $i/$currentRuns...")

                // Execute the test flow
                testFlow(driver, app, metricsManager, currentPlatform)

                // Reset app state for next iteration
                if (i < currentRuns) {
                    logger.info("🔄 Resetting app state for next iteration...")
                    try {
                        if (config.platform == Platform.ANDROID) {
                            (driver as AndroidDriver).terminateApp(packageId)
                            // Clear app data to ensure logout
                            try {
                                driver.executeScript(
                                    "mobile: clearApp",
                                    mapOf("appId" to packageId)
                                )
                            } catch (e: Exception) {
                                logger.warn("Failed to clear app data via mobile: clearApp, trying pm clear")
                                val args = listOf("clear", packageId)
                                (driver as AndroidDriver).executeScript(
                                    "mobile: shell",
                                    mapOf("command" to "pm", "args" to args)
                                )
                            }
                            (driver as AndroidDriver).activateApp(packageId)
                        } else {
                            (driver as IOSDriver).terminateApp(packageId)
                            driver.activateApp(packageId)
                        }
                        Thread.sleep(2000) // Wait for app to restart
                    } catch (e: Exception) {
                        logger.warn("⚠️ Failed to reset app state: ${e.message}")
                    }
                }
            }

            // Stop performance recording and parse trace
            metricsManager.stopPerformanceRecording()

            return metricsManager

        } finally {
            driver?.quit()
        }
    }

    private fun createDriver(app: App, platform: Platform): WebDriver {
        val appConfig =
            config.copy(app = app, platform = platform)
        return when (platform) {
            Platform.ANDROID -> AndroidDriverFactory.create(appConfig)
            Platform.IOS -> IOSDriverFactory.create(appConfig)
        }
    }
}
