package com.danioliveira.appium.perf

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.perf.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Main entrypoint for measuring performance of a single test case.
 * 
 * This is the Kotlin/Appium equivalent of Flashlight's `measurePerformance`.
 * 
 * **Example:**
 * ```kotlin
 * val result = measurePerformance(
 *     packageName = "com.example.app",
 *     testCase = TestCase(
 *         beforeTest = { login() },
 *         run = { navigateToProfile() }
 *     ),
 *     config = PerformanceConfig(platform = Platform.ANDROID)
 * )
 * 
 * result.writeAll(File("build/perf-results"))
 * ```
 */
suspend fun measurePerformance(
    packageName: String,
    testCase: TestCase,
    config: PerformanceConfig = PerformanceConfig(Platform.ANDROID),
    androidCollector: AndroidMetricsCollector? = null
): PerformanceResult {
    val logger = LoggerFactory.getLogger("PerformanceMeasurement")
    
    logger.info("=== Starting Performance Measurement ===")
    logger.info("Package: $packageName")
    logger.info("Platform: ${config.platform}")
    
    // Create or use provided collector
    val collector = androidCollector ?: if (config.platform == Platform.ANDROID) {
        AndroidMetricsCollector(packageName)
    } else {
        null
    }
    
    // Create session
    val session = PerformanceSession(packageName, config, collector)
    
    // Start session
    session.start()
    
    val testStartTime = System.currentTimeMillis()
    
    try {
        // Run before hook
        logger.info("Running beforeTest hook...")
        testCase.beforeTest()
        
        // Small delay to let things stabilize
        delay(500)
        
        // Run actual test
        logger.info("Running test...")
        testCase.run()
        
        // Wait for completion if duration specified
        testCase.durationMs?.let {
            logger.info("Waiting ${it}ms for test completion...")
            delay(it)
        } ?: run {
            // Default wait for things to settle
            delay(2000)
        }
        
    } finally {
        // Stop session and collect metrics
        val testEndTime = System.currentTimeMillis()
        val testDuration = testEndTime - testStartTime
        
        logger.info("Stopping performance session...")
        val sessionData = session.stop("performance_test")
        
        logger.info("=== Performance Measurement Complete ===")
        logger.info("Duration: ${testDuration}ms")
        
        // Build result - prefer Perfetto-derived metrics over ADB polling
        val metrics = buildPerformanceMetrics(sessionData)
        
        return PerformanceResult(
            testName = "performance_test",
            durationMs = testDuration,
            metrics = metrics,
            traceFile = sessionData.traceFile,
            screenMetrics = sessionData.traceMetrics?.screenMetrics ?: emptyList(),
            fpsPerSecond = sessionData.traceMetrics?.fpsPerSecond ?: emptyList()
        )
    }
}

/**
 * Main entrypoint for measuring performance of a multi-flow scenario with iterations.
 * 
 * **Example:**
 * ```kotlin
 * val result = measureScenario(
 *     packageName = "com.example.app",
 *     scenario = Scenario(
 *         name = "User Journey",
 *         iterations = 3,
 *         flows = listOf(
 *             Flow(
 *                 name = "Login",
 *                 steps = { performLogin() },
 *                 expectedScreens = listOf("LoginScreen", "TasksScreen")
 *             ),
 *             Flow(
 *                 name = "Create Task",
 *                 steps = { createNewTask() },
 *                 expectedScreens = listOf("CreateTaskScreen")
 *             )
 *         )
 *     ),
 *     config = PerformanceConfig(platform = Platform.ANDROID)
 * )
 * 
 * result.writeAll(File("build/perf-results"))
 * ```
 */
suspend fun measureScenario(
    packageName: String,
    scenario: Scenario,
    config: PerformanceConfig = PerformanceConfig(Platform.ANDROID),
    androidCollector: AndroidMetricsCollector? = null
): ScenarioResult {
    val logger = LoggerFactory.getLogger("PerformanceMeasurement")
    
    logger.info("=== Starting Scenario Measurement ===")
    logger.info("Scenario: ${scenario.name}")
    logger.info("Iterations: ${scenario.iterations}")
    logger.info("Flows: ${scenario.flows.size}")
    
    // Create or use provided collector
    val collector = androidCollector ?: if (config.platform == Platform.ANDROID) {
        AndroidMetricsCollector(packageName)
    } else {
        null
    }
    
    val allFlowResults = mutableListOf<FlowIterationResult>()
    val traceFiles = mutableListOf<java.io.File>()
    
    // Run iterations
    for (iteration in 1..scenario.iterations) {
        logger.info("=== Iteration $iteration/${scenario.iterations} ===")
        
        // Create session for this iteration
        val session = PerformanceSession(packageName, config, collector)
        session.start()
        
        val iterationStartTime = System.currentTimeMillis()
        val traceStartTime = System.currentTimeMillis()
        
        // Track flow execution data
        data class FlowExecutionData(
            val flow: Any,
            val startTimeMs: Long,
            val endTimeMs: Long,
            val actionResults: List<com.danioliveira.appium.perf.core.ActionResult>
        )
        
        val flowExecutions = mutableListOf<FlowExecutionData>()
        
        try {
            // Run all flows in sequence
            for (flow in scenario.flows) {
                when (flow) {
                    is com.danioliveira.appium.perf.core.Flow -> {
                        // Regular flow without action tracking
                        logger.info("Running flow: ${flow.name}")
                        
                        val flowStartTime = System.currentTimeMillis()
                        
                        // Execute flow steps
                        flow.steps()
                        
                        // Wait for completion if duration specified
                        flow.durationMs?.let {
                            logger.info("  Waiting ${it}ms for flow completion...")
                            delay(it)
                        } ?: run {
                            // Default wait
                            delay(1000)
                        }
                        
                        val flowEndTime = System.currentTimeMillis()
                        val flowDuration = flowEndTime - flowStartTime
                        
                        logger.info("  Flow completed in ${flowDuration}ms")
                        
                        flowExecutions.add(FlowExecutionData(flow, flowStartTime, flowEndTime, emptyList()))
                    }
                    
                    is com.danioliveira.appium.perf.core.FlowWithActions -> {
                        // Flow with action tracking
                        logger.info("Running flow with actions: ${flow.name}")
                        
                        val flowStartTime = System.currentTimeMillis()
                        val actionResults = mutableListOf<com.danioliveira.appium.perf.core.ActionResult>()
                        
                        // Execute each action with before/after snapshots
                        for (action in flow.actions) {
                            val actionResult = executeActionWithSnapshots(
                                action = action,
                                androidCollector = collector,
                                traceStartTime = traceStartTime
                            )
                            actionResults.add(actionResult)
                            
                            logger.info("  Action '${action.name}' completed in ${actionResult.durationMs}ms")
                        }
                        
                        // Wait for completion if duration specified
                        flow.durationMs?.let {
                            logger.info("  Waiting ${it}ms for flow completion...")
                            delay(it)
                        }
                        
                        val flowEndTime = System.currentTimeMillis()
                        val flowDuration = flowEndTime - flowStartTime
                        
                        logger.info("  Flow with ${actionResults.size} actions completed in ${flowDuration}ms")
                        
                        flowExecutions.add(FlowExecutionData(flow, flowStartTime, flowEndTime, actionResults))
                    }
                }
            }
            
        } finally {
            // Stop session
            val sessionData = session.stop("scenario_${scenario.name}_iter$iteration")
            
            sessionData.traceFile?.let { traceFiles.add(it) }
            
            // Enhance action metrics with systrace data if available
            flowExecutions.forEach { execution ->
                if (execution.actionResults.isNotEmpty()) {
                    enhanceActionMetricsWithSystrace(sessionData.traceMetrics, execution.actionResults)
                }
            }
            
            // Build flow results from execution data
            for (execution in flowExecutions) {
                val flow = execution.flow
                val flowName = when (flow) {
                    is com.danioliveira.appium.perf.core.Flow -> flow.name
                    is com.danioliveira.appium.perf.core.FlowWithActions -> flow.name
                    else -> "Unknown"
                }
                
                val expectedScreens = when (flow) {
                    is com.danioliveira.appium.perf.core.Flow -> flow.expectedScreens
                    is com.danioliveira.appium.perf.core.FlowWithActions -> flow.expectedScreens
                    else -> emptyList()
                }
                
                // Segment by expected screens
                val screenResults = if (expectedScreens.isNotEmpty()) {
                    val segments = session.segmentByScreens(sessionData, expectedScreens)
                    segments.map { segment ->
                        ScreenResult(
                            screenName = segment.screenName,
                            startTimeMs = segment.startTimeMs,
                            endTimeMs = segment.endTimeMs,
                            durationMs = segment.durationMs,
                            metrics = segment.metrics
                        )
                    }
                } else {
                    emptyList()
                }
                
                // Build flow result - prefer Perfetto-derived metrics over ADB polling
                val flowMetrics = buildPerformanceMetrics(sessionData)
                
                allFlowResults.add(
                    FlowIterationResult(
                        flowName = flowName,
                        iteration = iteration,
                        startTimeMs = execution.startTimeMs,
                        endTimeMs = execution.endTimeMs,
                        durationMs = execution.endTimeMs - execution.startTimeMs,
                        metrics = flowMetrics,
                        screenResults = screenResults,
                        actionResults = execution.actionResults
                    )
                )
            }
        }
    }
    
    // Aggregate metrics across iterations
    val cpuStats = allFlowResults.map { it.metrics.cpu }
    val memoryStats = allFlowResults.map { it.metrics.memory }
    val fpsStats = allFlowResults.map { it.metrics.fps }
    
    val aggregatedMetrics = AggregatedMetrics(
        cpu = AggregatedStats.from(cpuStats),
        memory = AggregatedStats.from(memoryStats),
        fps = AggregatedStats.from(fpsStats)
    )
    
    logger.info("=== Scenario Measurement Complete ===")
    logger.info("Total iterations: ${scenario.iterations}")
    logger.info("Total flows: ${allFlowResults.size}")
    
    return ScenarioResult(
        scenarioName = scenario.name,
        iterations = scenario.iterations,
        flowResults = allFlowResults,
        aggregatedMetrics = aggregatedMetrics,
        traceFiles = traceFiles
    )
}

/**
 * Execute an action with before/after ADB snapshots.
 * 
 * @param action The action to execute
 * @param androidCollector The Android metrics collector for ADB commands
 * @param traceStartTime The start time of the trace (for relative timestamps)
 * @return ActionResult with metrics from ADB snapshots
 */
private suspend fun executeActionWithSnapshots(
    action: com.danioliveira.appium.perf.core.Action,
    androidCollector: AndroidMetricsCollector?,
    traceStartTime: Long
): com.danioliveira.appium.perf.core.ActionResult {
    val startTime = System.currentTimeMillis()
    
    // Capture BEFORE snapshot
    val beforeSnapshot = captureAdbSnapshot(androidCollector)
    
    // Execute action
    action.execute()
    
    val endTime = System.currentTimeMillis()
    
    // Capture AFTER snapshot
    val afterSnapshot = captureAdbSnapshot(androidCollector)
    
    // Calculate initial metrics (will be enhanced with systrace later)
    return com.danioliveira.appium.perf.core.ActionResult(
        actionName = action.name,
        startTimeMs = startTime - traceStartTime,
        endTimeMs = endTime - traceStartTime,
        durationMs = endTime - startTime,
        metrics = com.danioliveira.appium.perf.core.ActionMetrics(
            cpu = com.danioliveira.appium.perf.core.CpuMetricsWithSource(
                avg = (beforeSnapshot.cpu + afterSnapshot.cpu) / 2.0,
                peak = maxOf(beforeSnapshot.cpu, afterSnapshot.cpu),
                samples = 2,
                source = "adb" // Will be updated to "systrace" if available
            ),
            memoryMb = afterSnapshot.memory,
            memoryDeltaMb = afterSnapshot.memory - beforeSnapshot.memory,
            fps = (beforeSnapshot.fps + afterSnapshot.fps) / 2.0,
            sources = mutableMapOf("cpu" to "adb", "memory" to "adb", "fps" to "adb")
        )
    )
}

/**
 * Capture a snapshot of current ADB metrics.
 */
private fun captureAdbSnapshot(collector: AndroidMetricsCollector?): AdbSnapshot {
    return if (collector != null) {
        try {
            AdbSnapshot(
                cpu = collector.collectCpuInfo().cpuPercentage,
                memory = collector.collectMemInfo().rssMb,
                fps = collector.collectGfxInfo().fps
            )
        } catch (e: Exception) {
            LoggerFactory.getLogger("PerformanceMeasurement")
                .warn("Failed to capture ADB snapshot: ${e.message}")
            AdbSnapshot(0.0, 0, 0.0)
        }
    } else {
        AdbSnapshot(0.0, 0, 0.0)
    }
}

/**
 * Enhance action metrics with systrace CPU data.
 * Replaces ADB CPU data with more accurate systrace data when available.
 */
private fun enhanceActionMetricsWithSystrace(
    traceMetrics: com.danioliveira.appium.metrics.android.TraceMetrics?,
    actionResults: List<com.danioliveira.appium.perf.core.ActionResult>
) {
    if (traceMetrics == null) return
    
    val logger = LoggerFactory.getLogger("PerformanceMeasurement")
    
    actionResults.forEach { action ->
        // Try to get CPU from systrace for this action's time window
        val cpuInWindow = traceMetrics.cpuUtilization
            .filter { it.timestampMs in action.startTimeMs..action.endTimeMs }
        
        if (cpuInWindow.isNotEmpty()) {
            // Update with systrace CPU data
            action.metrics.cpu = com.danioliveira.appium.perf.core.CpuMetricsWithSource(
                avg = cpuInWindow.map { it.cpuPercent }.average(),
                peak = cpuInWindow.maxOf { it.cpuPercent },
                samples = cpuInWindow.size,
                source = "systrace"
            )
            action.metrics.sources["cpu"] = "systrace"
            
            logger.debug("Enhanced action '${action.actionName}' with systrace CPU data " +
                "(${cpuInWindow.size} samples)")
        }
    }
}

/**
 * ADB snapshot data class.
 */
private data class AdbSnapshot(
    val cpu: Double,
    val memory: Int,
    val fps: Double
)

/**
 * Blocking version of measurePerformance for Java/JUnit compatibility.
 */
fun measurePerformanceBlocking(
    packageName: String,
    testCase: TestCase,
    config: PerformanceConfig = PerformanceConfig(Platform.ANDROID),
    androidCollector: AndroidMetricsCollector? = null
): PerformanceResult = runBlocking {
    measurePerformance(packageName, testCase, config, androidCollector)
}

/**
 * Blocking version of measureScenario for Java/JUnit compatibility.
 */
fun measureScenarioBlocking(
    packageName: String,
    scenario: Scenario,
    config: PerformanceConfig = PerformanceConfig(Platform.ANDROID),
    androidCollector: AndroidMetricsCollector? = null
): ScenarioResult = runBlocking {
    measureScenario(packageName, scenario, config, androidCollector)
}

/**
 * Build PerformanceMetrics from SessionData, preferring Perfetto-derived samples over ADB polling.
 * 
 * Priority:
 * 1. Perfetto trace metrics (most accurate, from SQL queries)
 * 2. Systrace metrics (fallback for older devices)
 * 3. ADB polling stats (last resort)
 */
private fun buildPerformanceMetrics(sessionData: SessionData): PerformanceMetrics {
    val logger = LoggerFactory.getLogger("PerformanceMeasurement")
    
    // Check if we have Perfetto/Systrace data with CPU utilization
    val traceMetrics = sessionData.traceMetrics
    val hasPerfettoData = traceMetrics != null && traceMetrics.cpuUtilization.isNotEmpty()
    
    if (hasPerfettoData && traceMetrics != null) {
        logger.info("Using Perfetto-derived metrics (${traceMetrics.cpuUtilization.size} CPU samples, ${traceMetrics.frameCount} frames)")
        
        // Extract CPU statistics from Perfetto trace
        val cpuValues = traceMetrics.cpuUtilization.map { it.cpuPercent }
        val cpuStats = if (cpuValues.isNotEmpty()) {
            MetricStats.from(cpuValues)
        } else {
            MetricStats.empty()
        }
        
        // Extract memory statistics from ADB (Perfetto memory is less reliable for RSS)
        val memoryStats = sessionData.adbStats?.memory?.let {
            MetricStats(
                min = it.min.toDouble(),
                max = it.max.toDouble(),
                avg = it.avg,
                p50 = it.avg,
                p90 = it.max.toDouble(),
                stddev = 0.0,
                samples = it.samples
            )
        } ?: MetricStats.empty()
        
        // Use FPS from Perfetto trace
        val fpsStats = if (traceMetrics.fps > 0) {
            MetricStats(
                min = traceMetrics.fps,
                max = traceMetrics.fps,
                avg = traceMetrics.fps,
                p50 = traceMetrics.fps,
                p90 = traceMetrics.fps,
                stddev = 0.0,
                samples = traceMetrics.frameCount
            )
        } else {
            MetricStats.empty()
        }
        
        return PerformanceMetrics(
            cpu = cpuStats,
            memory = memoryStats,
            fps = fpsStats
        )
    }
    
    // Fallback to ADB polling stats
    logger.info("Using ADB polling metrics (no Perfetto data available)")
    
    return PerformanceMetrics(
        cpu = sessionData.adbStats?.cpu?.let {
            MetricStats(
                min = it.min.toDouble(),
                max = it.max.toDouble(),
                avg = it.avg,
                p50 = it.avg,
                p90 = it.max.toDouble(),
                stddev = 0.0,
                samples = it.samples
            )
        } ?: MetricStats.empty(),
        memory = sessionData.adbStats?.memory?.let {
            MetricStats(
                min = it.min.toDouble(),
                max = it.max.toDouble(),
                avg = it.avg,
                p50 = it.avg,
                p90 = it.max.toDouble(),
                stddev = 0.0,
                samples = it.samples
            )
        } ?: MetricStats.empty(),
        fps = sessionData.adbStats?.fps?.let {
            MetricStats(
                min = it.min.toDouble(),
                max = it.max.toDouble(),
                avg = it.avg,
                p50 = it.avg,
                p90 = it.max.toDouble(),
                stddev = 0.0,
                samples = it.samples
            )
        } ?: MetricStats.empty()
    )
}


