package com.danioliveira.appium.metrics

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.metrics.ios.IOSMetricsCollector
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class MetricsManager(
    private val platform: Platform,
    private val packageOrBundleId: String,
    private val udid: String? = null,
    private val driver: WebDriver? = null,
    private val instrumentsProfileName: String = "Activity Monitor"  // iOS Instruments profile to use
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val androidCollector = if (platform == Platform.ANDROID) {
        try {
            AndroidMetricsCollector(packageOrBundleId).also {
                logger.info("✅ Android metrics collector initialized for: $packageOrBundleId")
            }
        } catch (e: IllegalStateException) {
            logger.error("❌ Failed to initialize Android metrics collector: ${e.message}")
            logger.error("   All metrics will be zeros. Please fix ADB setup to collect real metrics.")
            null
        }
    } else null
    
    private val iosCollector = if (platform == Platform.IOS) {
        IOSMetricsCollector(
            packageOrBundleId, 
            udid,
            driver as? IOSDriver,
            instrumentsProfileName
        ).also {
            logger.info("✅ iOS metrics collector initialized for: $packageOrBundleId")
            logger.info("   Using Instruments profile: '$instrumentsProfileName'")
        }
    } else null
    
    private val actionMetrics = mutableListOf<ActionMetrics>()
    private val pageMetricsMap = ConcurrentHashMap<String, MutableList<ActionMetrics>>()
    
    private var appLaunchTimeMs: Long = 0
    
    fun measureAppLaunchTime(): Long {
        appLaunchTimeMs = when (platform) {
            Platform.ANDROID -> androidCollector?.measureLaunchTime() ?: 0L
            Platform.IOS -> iosCollector?.measureLaunchTime() ?: 0L
        }
        logger.info("App launch time measured: ${appLaunchTimeMs}ms")
        return appLaunchTimeMs
    }
    
    /**
     * Start performance recording (iOS only, uses instruments)
     * Call this at the beginning of your test suite
     */
    fun startPerformanceRecording() {
        if (platform == Platform.IOS) {
            iosCollector?.startPerformanceRecording()
        }
    }
    
    /**
     * Stop performance recording (iOS only)
     * Call this at the end of your test suite
     * Returns performance snapshot with recording duration
     */
    fun stopPerformanceRecording(): String? {
        if (platform == Platform.IOS) {
            val snapshot = iosCollector?.stopPerformanceRecording()
            
            // Update all collected metrics with parsed values from trace
            iosCollector?.let { collector ->
                updateMetricsWithParsedValues(collector)
            }
            
            return snapshot?.message
        }
        return null
    }
    
    /**
     * Update all collected metrics with real values from parsed trace file
     * This replaces placeholder 0 values with actual parsed metrics
     */
    private fun updateMetricsWithParsedValues(collector: IOSMetricsCollector) {
        val parsedMetrics = collector.getLastParsedMetrics() ?: return
        
        logger.info("Updating ${actionMetrics.size} action metrics with parsed trace values...")
        logger.info("  Parsed CPU: avg=${String.format("%.1f", parsedMetrics.avgCpuPercent)}%, peak=${String.format("%.1f", parsedMetrics.peakCpuPercent)}%")
        logger.info("  Parsed Memory: avg=${parsedMetrics.avgMemoryMb}MB, peak=${parsedMetrics.peakMemoryMb}MB")
        
        // Update all action metrics with the parsed values
        actionMetrics.forEachIndexed { index, metrics ->
            actionMetrics[index] = metrics.copy(
                memoryMb = parsedMetrics.avgMemoryMb,
                cpuPercent = parsedMetrics.avgCpuPercent
            )
        }
        
        // Update page metrics map
        pageMetricsMap.forEach { (pageName, metricsList) ->
            pageMetricsMap[pageName] = metricsList.map { metrics ->
                metrics.copy(
                    memoryMb = parsedMetrics.avgMemoryMb,
                    cpuPercent = parsedMetrics.avgCpuPercent
                )
            }.toMutableList()
        }
        
        logger.info("✅ All metrics updated with real trace values")
    }
    
    fun trackAction(pageName: String, actionName: String, block: () -> Unit) {
        logger.debug("Starting metrics collection for $pageName.$actionName")
        
        val startTime = System.currentTimeMillis()
        
        // Reset frame stats for Android
        androidCollector?.resetGfxInfo()
        
        // Execute the action
        block()
        
        // Small delay to let metrics settle
        Thread.sleep(100)
        
        val durationMs = System.currentTimeMillis() - startTime
        
        // Collect metrics after action
        val metrics = collectMetrics(pageName, actionName, durationMs)
        
        // Store metrics
        actionMetrics.add(metrics)
        pageMetricsMap.getOrPut(pageName) { mutableListOf() }.add(metrics)
        
        logger.info("Metrics collected for $pageName.$actionName: " +
            "duration=${durationMs}ms, memory=${metrics.memoryMb}MB, cpu=${metrics.cpuPercent}%")
    }
    
    private fun collectMetrics(
        pageName: String, 
        actionName: String, 
        durationMs: Long
    ): ActionMetrics {
        return when (platform) {
            Platform.ANDROID -> collectAndroidMetrics(pageName, actionName, durationMs)
            Platform.IOS -> collectIOSMetrics(pageName, actionName, durationMs)
        }
    }
    
    private fun collectAndroidMetrics(
        pageName: String,
        actionName: String,
        durationMs: Long
    ): ActionMetrics {
        if (androidCollector == null) {
            logger.warn("⚠️  Android collector not available. Returning empty metrics.")
            return ActionMetrics(
                pageName = pageName,
                actionName = actionName,
                durationMs = durationMs,
                memoryMb = 0,
                cpuPercent = 0.0,
                avgFrameTimeMs = 0.0,
                jankPercentage = 0.0,
                fps = 0.0,
                platform = platform.name
            )
        }
        
        val gfxInfo = androidCollector.collectGfxInfo()
        val memInfo = androidCollector.collectMemInfo()
        val cpuInfo = androidCollector.collectCpuInfo()
        
        // Log warning if metrics are all zeros (likely ADB issue)
        if (memInfo.rssMb == 0 && cpuInfo.cpuPercentage == 0.0) {
            logger.warn("⚠️  Metrics are zeros for $pageName.$actionName - ADB may not be working properly")
        }
        
        return ActionMetrics(
            pageName = pageName,
            actionName = actionName,
            durationMs = durationMs,
            memoryMb = memInfo.rssMb,
            cpuPercent = cpuInfo.cpuPercentage,
            avgFrameTimeMs = gfxInfo.avgFrameTimeMs,
            jankPercentage = gfxInfo.jankPercentage,
            fps = if (gfxInfo.avgFrameTimeMs > 0) {
                1000.0 / gfxInfo.avgFrameTimeMs
            } else 60.0,
            platform = platform.name
        )
    }
    
    private fun collectIOSMetrics(
        pageName: String,
        actionName: String,
        durationMs: Long
    ): ActionMetrics {
        val memInfo = iosCollector?.collectMemoryInfo()
        val cpuInfo = iosCollector?.collectCpuInfo()
        val renderInfo = iosCollector?.collectRenderingMetrics()
        
        return ActionMetrics(
            pageName = pageName,
            actionName = actionName,
            durationMs = durationMs,
            memoryMb = memInfo?.peakMemoryMb ?: 0,
            cpuPercent = cpuInfo?.avgCpuPercent ?: 0.0,
            avgFrameTimeMs = renderInfo?.avgFrameTimeMs ?: 16.67,
            jankPercentage = 0.0, // iOS doesn't have direct jank metric
            fps = renderInfo?.fps ?: 60.0,
            platform = platform.name
        )
    }
    
    
    fun getPageSummary(pageName: String): PageMetricsSummary? {
        val metrics = pageMetricsMap[pageName] ?: return null
        if (metrics.isEmpty()) return null
        
        return PageMetricsSummary(
            pageName = pageName,
            actionCount = metrics.size,
            totalDurationMs = metrics.sumOf { it.durationMs },
            avgDurationMs = metrics.map { it.durationMs }.average(),
            avgMemoryMb = metrics.map { it.memoryMb }.average(),
            peakMemoryMb = metrics.maxOf { it.memoryMb },
            avgCpuPercent = metrics.map { it.cpuPercent }.average(),
            peakCpuPercent = metrics.maxOf { it.cpuPercent },
            avgFrameTimeMs = metrics.map { it.avgFrameTimeMs }.average(),
            avgJankPercentage = metrics.map { it.jankPercentage }.average(),
            avgFps = metrics.map { it.fps }.average(),
            actions = metrics.toList()
        )
    }
    
    fun getAllPageSummaries(): List<PageMetricsSummary> {
        return pageMetricsMap.keys.mapNotNull { getPageSummary(it) }
    }
    
    fun getTotalSummary(): TotalMetricsSummary {
        val allMetrics = actionMetrics
        
        return TotalMetricsSummary(
            appLaunchTimeMs = appLaunchTimeMs,
            totalActions = allMetrics.size,
            totalDurationMs = allMetrics.sumOf { it.durationMs },
            avgActionDurationMs = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.durationMs }.average()
            } else 0.0,
            avgMemoryMb = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.memoryMb }.average()
            } else 0.0,
            peakMemoryMb = allMetrics.maxOfOrNull { it.memoryMb } ?: 0,
            avgCpuPercent = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.cpuPercent }.average()
            } else 0.0,
            peakCpuPercent = allMetrics.maxOfOrNull { it.cpuPercent } ?: 0.0,
            avgFrameTimeMs = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.avgFrameTimeMs }.average()
            } else 0.0,
            avgJankPercentage = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.jankPercentage }.average()
            } else 0.0,
            avgFps = if (allMetrics.isNotEmpty()) {
                allMetrics.map { it.fps }.average()
            } else 60.0,
            platform = platform.name,
            pageCount = pageMetricsMap.size,
            pageSummaries = getAllPageSummaries()
        )
    }
    
    fun reset() {
        actionMetrics.clear()
        pageMetricsMap.clear()
        appLaunchTimeMs = 0
        logger.info("Metrics manager reset")
    }
}

data class ActionMetrics(
    val pageName: String,
    val actionName: String,
    val durationMs: Long,
    val memoryMb: Int,
    val cpuPercent: Double,
    val avgFrameTimeMs: Double,
    val jankPercentage: Double,
    val fps: Double,
    val platform: String
)

data class PageMetricsSummary(
    val pageName: String,
    val actionCount: Int,
    val totalDurationMs: Long,
    val avgDurationMs: Double,
    val avgMemoryMb: Double,
    val peakMemoryMb: Int,
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgFrameTimeMs: Double,
    val avgJankPercentage: Double,
    val avgFps: Double,
    val actions: List<ActionMetrics>
)

data class TotalMetricsSummary(
    val appLaunchTimeMs: Long,
    val totalActions: Int,
    val totalDurationMs: Long,
    val avgActionDurationMs: Double,
    val avgMemoryMb: Double,
    val peakMemoryMb: Int,
    val avgCpuPercent: Double,
    val peakCpuPercent: Double,
    val avgFrameTimeMs: Double,
    val avgJankPercentage: Double,
    val avgFps: Double,
    val platform: String,
    val pageCount: Int,
    val pageSummaries: List<PageMetricsSummary>
)

