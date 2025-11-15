package com.danioliveira.appium.perf.fps

import com.danioliveira.appium.metrics.android.FlashlightFpsCollector
import com.danioliveira.appium.metrics.android.FlashlightFpsMetrics

/**
 * Wrapper for Flashlight gfxinfo-based FPS collection.
 * 
 * This uses the gfxinfo approach from Flashlight:
 * https://github.com/bamlab/flashlight/blob/main/packages/platforms/android/src/commands/gfxInfo/pollFpsUsage.ts
 * 
 * **Pros:**
 * - Real-time polling (works during test execution)
 * - Accounts for idle time
 * - No trace parsing required
 * 
 * **Cons:**
 * - Less accurate than atrace (no frame-level detail)
 * - Requires HWUI profiling enabled
 */
class FlashlightGfxInfo(packageName: String) {
    private val collector = FlashlightFpsCollector(packageName)
    
    /**
     * Collect current FPS metrics.
     */
    fun collect(): FlashlightFpsMetrics {
        return collector.collectFps()
    }
}




