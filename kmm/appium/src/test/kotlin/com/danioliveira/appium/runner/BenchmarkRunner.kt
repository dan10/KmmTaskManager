package com.danioliveira.appium.runner

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.drivers.IOSDriverFactory
import com.danioliveira.appium.metrics.ImprovedMetricsManager
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.net.URL

class BenchmarkRunner(
    private val config: BenchmarkConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun runTestSuite(
        app: App,
        frameworkName: String,
        runsOverride: Int? = null,
        warmupOverride: Int? = null,
        testFlow: (WebDriver, App, MetricsManager) -> Unit
    ): MetricsManager {
        val currentRuns = runsOverride ?: config.runs
        val currentWarmup = warmupOverride ?: config.warmup

        logger.info("\n📊 Running test suite for $frameworkName")

        val driver = createDriver(app)
        val packageId = when (app) {
            App.KMM -> config.packageName ?: "com.danioliveira.taskmanager"
            App.FLUTTER -> config.packageName ?: "com.example.task_manager_app"
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
                    testFlow(driver, app, metricsManager)
                    Thread.sleep(2000) // Brief pause between iterations
                }
                metricsManager.reset() // Clear warmup data
                logger.info("✅ Warmup complete, starting measured runs")
            }

            // Measured runs
            for (i in 1..currentRuns) {
                logger.info("Running iteration $i/$currentRuns...")

                // Reset metrics for this iteration
                metricsManager.reset()

                // Execute the test flow
                testFlow(driver, app, metricsManager)

                Thread.sleep(200) // Brief pause between iterations
            }

            // Stop performance recording and parse trace
            metricsManager.stopPerformanceRecording()

            return metricsManager

        } finally {
            driver.quit()
        }
    }

    private fun createDriver(app: App): WebDriver {
        val appConfig = config.copy(app = app)
        return when (config.platform) {
            Platform.ANDROID -> AndroidDriverFactory.create(appConfig)
            Platform.IOS -> IOSDriverFactory.create(appConfig)
        }
    }
}
