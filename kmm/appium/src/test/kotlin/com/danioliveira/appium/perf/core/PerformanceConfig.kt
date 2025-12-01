package com.danioliveira.appium.perf.core

import com.danioliveira.appium.config.Platform

/**
 * Configuration for performance measurement.
 *
 * @property platform Target platform (Android or iOS)
 * @property usePerfetto Use Perfetto instead of Systrace (Android only, default: true)
 * @property pollingIntervalMs Interval for ADB/Instruments polling (default: 500ms)
 * @property systraceBufferSizeKb Buffer size for systrace/perfetto (default: 65536 = 64MB)
 * @property systraceCategories Categories to capture in systrace/perfetto
 * @property enableCpuProfiling Enable detailed CPU profiling
 * @property enableMemoryProfiling Enable detailed memory profiling
 * @property enableFpsProfiling Enable FPS measurement
 * @property exportTimeSeries Export detailed time series data
 */
data class PerformanceConfig(
    val platform: Platform,
    val usePerfetto: Boolean = true,  // Use Perfetto instead of Systrace (Android only)
    val pollingIntervalMs: Long = 500,
    val systraceBufferSizeKb: Int = 65536,
    val systraceCategories: List<String> = DEFAULT_SYSTRACE_CATEGORIES,
    val enableCpuProfiling: Boolean = true,
    val enableMemoryProfiling: Boolean = true,
    val enableFpsProfiling: Boolean = true,
    val exportTimeSeries: Boolean = false
) {
    companion object {
        val DEFAULT_SYSTRACE_CATEGORIES = listOf(
            "sched",   // CPU scheduling (REQUIRED)
            "freq",    // CPU frequency
            "idle",    // CPU idle states
            "am",      // Activity Manager
            "wm",      // Window Manager
            "gfx",     // Graphics
            "view",    // View system (REQUIRED for Choreographer)
            "binder_driver",
            "hal",
            "dalvik",
            "input",
            "res",
            "sync",
            "memory"
        )
    }
}



