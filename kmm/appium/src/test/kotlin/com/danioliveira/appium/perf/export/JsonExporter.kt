package com.danioliveira.appium.perf.export

import com.danioliveira.appium.perf.core.*
import com.google.gson.GsonBuilder
import java.io.File

/**
 * Exports performance results to JSON format.
 */
object JsonExporter {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    /**
     * Export a single performance result to JSON.
     */
    fun exportPerformanceResult(result: PerformanceResult, outputFile: File) {
        val data = mutableMapOf<String, Any?>(
            "testName" to result.testName,
            "durationMs" to result.durationMs,
            "traceFile" to result.traceFile?.absolutePath,
            "traceType" to when (result.traceFile?.extension) {
                "pftrace" -> "perfetto"
                "html" -> "systrace"
                else -> "unknown"
            },
            "metrics" to mapOf(
                "cpu" to formatMetricStats(result.metrics.cpu),
                "memory" to formatMetricStats(result.metrics.memory),
                "fps" to formatMetricStats(result.metrics.fps)
            )
        )
        
        // Add jank stats if available
        if (result.jankStats != null) {
            data["jankStats"] = mapOf(
                "totalFrames" to result.jankStats.totalFrames,
                "jankFrames" to result.jankStats.jankFrames,
                "jankPercentage" to result.jankStats.jankPercentage,
                "avgFrameTimeMs" to result.jankStats.avgFrameTimeMs,
                "maxFrameTimeMs" to result.jankStats.maxFrameTimeMs,
                "p50FrameTimeMs" to result.jankStats.p50FrameTimeMs,
                "p90FrameTimeMs" to result.jankStats.p90FrameTimeMs,
                "p95FrameTimeMs" to result.jankStats.p95FrameTimeMs,
                "p99FrameTimeMs" to result.jankStats.p99FrameTimeMs
            )
        }
        
        // Add startup breakdown if available
        if (result.startupBreakdown != null) {
            data["startupBreakdown"] = mapOf(
                "type" to result.startupBreakdown.type,
                "totalMs" to result.startupBreakdown.totalMs,
                "phases" to result.startupBreakdown.phases.map { phase ->
                    mapOf(
                        "name" to phase.name,
                        "durationMs" to phase.durationMs,
                        "percentageOfTotal" to phase.percentageOfTotal
                    )
                }
            )
        }
        
        val json = gson.toJson(data)
        outputFile.writeText(json)
    }
    
    /**
     * Export scenario result to JSON.
     */
    fun exportScenarioResult(result: ScenarioResult, outputFile: File) {
        val json = gson.toJson(mapOf(
            "scenarioName" to result.scenarioName,
            "iterations" to result.iterations,
            "traceFiles" to result.traceFiles.map { it.absolutePath },
            "aggregatedMetrics" to mapOf(
                "cpu" to formatAggregatedStats(result.aggregatedMetrics.cpu),
                "memory" to formatAggregatedStats(result.aggregatedMetrics.memory),
                "fps" to formatAggregatedStats(result.aggregatedMetrics.fps)
            ),
            "flows" to result.flowResults.map { flow ->
                val flowData = mutableMapOf<String, Any?>(
                    "flowName" to flow.flowName,
                    "iteration" to flow.iteration,
                    "startTimeMs" to flow.startTimeMs,
                    "endTimeMs" to flow.endTimeMs,
                    "durationMs" to flow.durationMs,
                    "metrics" to mapOf(
                        "cpu" to formatMetricStats(flow.metrics.cpu),
                        "memory" to formatMetricStats(flow.metrics.memory),
                        "fps" to formatMetricStats(flow.metrics.fps)
                    ),
                    "screens" to flow.screenResults.map { screen ->
                        mapOf(
                            "screenName" to screen.screenName,
                            "startTimeMs" to screen.startTimeMs,
                            "endTimeMs" to screen.endTimeMs,
                            "durationMs" to screen.durationMs,
                            "metrics" to mapOf(
                                "cpu" to formatMetricStats(screen.metrics.cpu),
                                "memory" to formatMetricStats(screen.metrics.memory),
                                "fps" to formatMetricStats(screen.metrics.fps)
                            )
                        )
                    }
                )
                
                // Add jank stats if available
                if (flow.jankStats != null) {
                    flowData["jankStats"] = mapOf(
                        "totalFrames" to flow.jankStats.totalFrames,
                        "jankFrames" to flow.jankStats.jankFrames,
                        "jankPercentage" to flow.jankStats.jankPercentage,
                        "p50FrameTimeMs" to flow.jankStats.p50FrameTimeMs,
                        "p90FrameTimeMs" to flow.jankStats.p90FrameTimeMs,
                        "p99FrameTimeMs" to flow.jankStats.p99FrameTimeMs
                    )
                }
                
                flowData
            }
        ))
        
        outputFile.writeText(json)
    }
    
    private fun formatMetricStats(stats: MetricStats) = mapOf(
        "min" to stats.min,
        "max" to stats.max,
        "avg" to stats.avg,
        "p50" to stats.p50,
        "p90" to stats.p90,
        "stddev" to stats.stddev,
        "samples" to stats.samples
    )
    
    private fun formatAggregatedStats(stats: AggregatedStats) = mapOf(
        "meanOfMeans" to stats.meanOfMeans,
        "minOfMins" to stats.minOfMins,
        "maxOfMaxes" to stats.maxOfMaxes,
        "stddevAcrossIterations" to stats.stddevAcrossIterations,
        "confidenceInterval95" to mapOf(
            "lower" to stats.confidenceInterval95.first,
            "upper" to stats.confidenceInterval95.second
        )
    )
}



