package com.danioliveira.appium.perf.core

import com.danioliveira.appium.metrics.android.perfetto.JankStats
import com.danioliveira.appium.metrics.android.perfetto.StartupBreakdown
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.io.File

/**
 * Result of a single performance test case.
 *
 * @property testName Name of the test
 * @property durationMs Total duration of the test
 * @property metrics Collected metrics
 * @property traceFile Path to the trace file (systrace or Instruments)
 * @property screenMetrics Per-screen performance metrics
 * @property fpsPerSecond Per-second FPS time series data
 */
data class PerformanceResult(
    val testName: String,
    val durationMs: Long,
    val metrics: PerformanceMetrics,
    val traceFile: File?,
    val jankStats: JankStats? = null,
    val startupBreakdown: StartupBreakdown? = null,
    val screenMetrics: List<com.danioliveira.appium.metrics.android.ScreenMetrics> = emptyList(),
    val fpsPerSecond: List<com.danioliveira.appium.metrics.android.FpsPerSecond> = emptyList()
) {
    /**
     * Export all results to the specified directory.
     */
    fun writeAll(outputDir: File) {
        outputDir.mkdirs()
        
        // Export to JSON
        val jsonFile = File(outputDir, "performance_result.json")
        com.danioliveira.appium.perf.export.JsonExporter.exportPerformanceResult(this, jsonFile)
        
        // Export to CSV
        val csvFile = File(outputDir, "performance_result.csv")
        com.danioliveira.appium.perf.export.CsvExporter.exportPerformanceResult(this, csvFile)
        
        // Export to Markdown
        val mdFile = File(outputDir, "performance_result.md")
        com.danioliveira.appium.perf.export.MarkdownExporter.exportPerformanceResult(this, mdFile)
        
        // Export per-screen metrics if available
        if (screenMetrics.isNotEmpty()) {
            val screensCsvFile = File(outputDir, "screen_metrics.csv")
            com.danioliveira.appium.perf.export.CsvExporter.exportScreenMetrics(screenMetrics, screensCsvFile)
            
            val screensMdFile = File(outputDir, "screen_metrics.md")
            com.danioliveira.appium.perf.export.MarkdownExporter.exportScreenMetrics(screenMetrics, screensMdFile)
        }
        
        // Export per-second FPS time series if available
        if (fpsPerSecond.isNotEmpty()) {
            val fpsTimeSeriesCsvFile = File(outputDir, "fps_time_series.csv")
            com.danioliveira.appium.perf.export.CsvExporter.exportFpsTimeSeries(fpsPerSecond, fpsTimeSeriesCsvFile)
        }
    }
}

/**
 * Result of a multi-flow scenario with multiple iterations.
 *
 * @property scenarioName Name of the scenario
 * @property iterations Number of iterations executed
 * @property flowResults Results for each flow in each iteration
 * @property aggregatedMetrics Aggregated metrics across all iterations
 * @property traceFiles Trace files for each iteration
 */
data class ScenarioResult(
    val scenarioName: String,
    val iterations: Int,
    val flowResults: List<FlowIterationResult>,
    val aggregatedMetrics: AggregatedMetrics,
    val traceFiles: List<File>
) {
    /**
     * Export all results to the specified directory.
     */
    fun writeAll(outputDir: File) {
        outputDir.mkdirs()
        
        // Export scenario summary
        val summaryJsonFile = File(outputDir, "scenario_summary.json")
        com.danioliveira.appium.perf.export.JsonExporter.exportScenarioResult(this, summaryJsonFile)
        
        val summaryMdFile = File(outputDir, "scenario_summary.md")
        com.danioliveira.appium.perf.export.MarkdownExporter.exportScenarioResult(this, summaryMdFile)
        
        // Export flows CSV
        val flowsCsvFile = File(outputDir, "flows.csv")
        com.danioliveira.appium.perf.export.CsvExporter.exportFlows(this, flowsCsvFile)
        
        // Export screens CSV
        val screensCsvFile = File(outputDir, "screens_in_flows.csv")
        com.danioliveira.appium.perf.export.CsvExporter.exportScreens(this, screensCsvFile)
        
        // Export actions CSV if any flows have actions
        if (flowResults.any { it.actionResults.isNotEmpty() }) {
            val actionsCsvFile = File(outputDir, "actions.csv")
            com.danioliveira.appium.perf.export.CsvExporter.exportActions(this, actionsCsvFile)
        }
    }
}

/**
 * Result for a single flow in a single iteration.
 */
data class FlowIterationResult(
    val flowName: String,
    val iteration: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val metrics: PerformanceMetrics,
    val screenResults: List<ScreenResult>,
    val actionResults: List<ActionResult> = emptyList(),
    val jankStats: JankStats? = null
)

/**
 * Result for a single screen within a flow.
 */
data class ScreenResult(
    val screenName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val metrics: PerformanceMetrics
)

/**
 * Result for a single action within a flow.
 * Contains metrics captured via before/after ADB snapshots and enhanced with systrace data.
 */
data class ActionResult(
    val actionName: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    var metrics: ActionMetrics
)

/**
 * Action-level metrics with source tracking.
 * Combines ADB snapshots (memory, FPS) with systrace data (CPU).
 */
data class ActionMetrics(
    var cpu: CpuMetricsWithSource,
    val memoryMb: Int,
    val memoryDeltaMb: Int,
    val fps: Double,
    val sources: MutableMap<String, String>
)

/**
 * CPU metrics with source tracking.
 */
data class CpuMetricsWithSource(
    val avg: Double,
    val peak: Double,
    val samples: Int,
    val source: String
)

/**
 * Performance metrics for a test, flow, or screen.
 */
data class PerformanceMetrics(
    val cpu: MetricStats,
    val memory: MetricStats,
    val fps: MetricStats
) {
    companion object {
        fun empty() = PerformanceMetrics(
            cpu = MetricStats.empty(),
            memory = MetricStats.empty(),
            fps = MetricStats.empty()
        )
    }
}

/**
 * Statistical metrics (min, max, avg, p90, p95, p99, stddev).
 *
 * NOTE:
 * - P50 is effectively the median and very close to avg for these workloads,
 *   so we omit it and focus on tail latency: p90, p95, p99.
 */
data class MetricStats(
    val min: Double,
    val max: Double,
    val avg: Double,
    val p90: Double,
    val p95: Double,
    val p99: Double,
    val stddev: Double,
    val samples: Int
) {
    companion object {
        fun empty() = MetricStats(
            min = 0.0,
            max = 0.0,
            avg = 0.0,
            p90 = 0.0,
            p95 = 0.0,
            p99 = 0.0,
            stddev = 0.0,
            samples = 0
        )
        
        fun from(values: List<Double>): MetricStats {
            if (values.isEmpty()) return empty()

            // Use Apache Commons Math for robust descriptive stats & percentiles
            val stats = DescriptiveStatistics()
            values.forEach { stats.addValue(it) }

            val min = stats.min
            val max = stats.max
            val avg = stats.mean
            val p90 = stats.getPercentile(90.0)
            val p95 = stats.getPercentile(95.0)
            val p99 = stats.getPercentile(99.0)
            val stddev = stats.standardDeviation

            return MetricStats(
                min = min,
                max = max,
                avg = avg,
                p90 = p90,
                p95 = p95,
                p99 = p99,
                stddev = stddev,
                samples = stats.n.toInt()
            )
        }
    }
}

/**
 * Aggregated metrics across multiple iterations.
 */
data class AggregatedMetrics(
    val cpu: AggregatedStats,
    val memory: AggregatedStats,
    val fps: AggregatedStats
)

/**
 * Aggregated statistics across iterations (mean of means, confidence ranges).
 */
data class AggregatedStats(
    val meanOfMeans: Double,
    val minOfMins: Double,
    val maxOfMaxes: Double,
    val stddevAcrossIterations: Double,
    val confidenceInterval95: Pair<Double, Double>
) {
    companion object {
        fun from(stats: List<MetricStats>): AggregatedStats {
            if (stats.isEmpty()) {
                return AggregatedStats(0.0, 0.0, 0.0, 0.0, 0.0 to 0.0)
            }
            
            val means = stats.map { it.avg }
            val meanOfMeans = means.average()
            val minOfMins = stats.minOf { it.min }
            val maxOfMaxes = stats.maxOf { it.max }
            val stddev = MetricStats.from(means).stddev
            
            // 95% confidence interval (assuming normal distribution)
            val marginOfError = 1.96 * stddev / kotlin.math.sqrt(stats.size.toDouble())
            val ci95 = (meanOfMeans - marginOfError) to (meanOfMeans + marginOfError)
            
            return AggregatedStats(meanOfMeans, minOfMins, maxOfMaxes, stddev, ci95)
        }
    }
}


