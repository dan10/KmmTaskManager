package com.danioliveira.appium.perf.export

import com.danioliveira.appium.perf.core.*
import java.io.File
import java.util.Locale

/**
 * Exports performance results to CSV format.
 */
object CsvExporter {
    
    /**
     * Export a single performance result to CSV.
     */
    fun exportPerformanceResult(result: PerformanceResult, outputFile: File) {
        val csv = buildString {
            appendLine("Metric,Min,Max,Avg,P50,P90,StdDev,Samples")
            appendLine(formatMetricRow("CPU (%)", result.metrics.cpu))
            appendLine(formatMetricRow("Memory (MB)", result.metrics.memory))
            appendLine(formatMetricRow("FPS", result.metrics.fps))
        }
        
        outputFile.writeText(csv)
    }
    
    /**
     * Export flows from scenario result to CSV.
     */
    fun exportFlows(result: ScenarioResult, outputFile: File) {
        val csv = buildString {
            appendLine("Flow,Iteration,Start (ms),End (ms),Duration (ms)," +
                "CPU Min,CPU Max,CPU Avg,CPU P90," +
                "Memory Min,Memory Max,Memory Avg,Memory P90," +
                "FPS Min,FPS Max,FPS Avg,FPS P90")
            
            result.flowResults.forEach { flow ->
                append("${flow.flowName},")
                append("${flow.iteration},")
                append("${flow.startTimeMs},")
                append("${flow.endTimeMs},")
                append("${flow.durationMs},")
                append(formatMetricValues(flow.metrics.cpu))
                append(",")
                append(formatMetricValues(flow.metrics.memory))
                append(",")
                appendLine(formatMetricValues(flow.metrics.fps))
            }
        }
        
        outputFile.writeText(csv)
    }
    
    /**
     * Export screens from scenario result to CSV.
     */
    fun exportScreens(result: ScenarioResult, outputFile: File) {
        val csv = buildString {
            appendLine("Flow,Screen,Iteration,Start (ms),End (ms),Duration (ms)," +
                "CPU Min,CPU Max,CPU Avg,CPU P90," +
                "Memory Min,Memory Max,Memory Avg,Memory P90," +
                "FPS Min,FPS Max,FPS Avg,FPS P90")
            
            result.flowResults.forEach { flow ->
                flow.screenResults.forEach { screen ->
                    append("${flow.flowName},")
                    append("${screen.screenName},")
                    append("${flow.iteration},")
                    append("${screen.startTimeMs},")
                    append("${screen.endTimeMs},")
                    append("${screen.durationMs},")
                    append(formatMetricValues(screen.metrics.cpu))
                    append(",")
                    append(formatMetricValues(screen.metrics.memory))
                    append(",")
                    appendLine(formatMetricValues(screen.metrics.fps))
                }
            }
        }
        
        outputFile.writeText(csv)
    }
    
    /**
     * Export actions from scenario result to CSV.
     */
    fun exportActions(result: ScenarioResult, outputFile: File) {
        val csv = buildString {
            appendLine("Flow,Action,Iteration,Start (ms),End (ms),Duration (ms)," +
                "CPU Avg (%),CPU Peak (%),CPU Source,CPU Samples," +
                "Memory (MB),Memory Delta (MB)," +
                "FPS,FPS Source")
            
            result.flowResults.forEach { flow ->
                flow.actionResults.forEach { action ->
                    append("${flow.flowName},")
                    append("${action.actionName},")
                    append("${flow.iteration},")
                    append("${action.startTimeMs},")
                    append("${action.endTimeMs},")
                    append("${action.durationMs},")
                    append("${fmt(action.metrics.cpu.avg)},")
                    append("${fmt(action.metrics.cpu.peak)},")
                    append("${action.metrics.sources["cpu"]},")
                    append("${action.metrics.cpu.samples},")
                    append("${action.metrics.memoryMb},")
                    append("${action.metrics.memoryDeltaMb},")
                    append("${fmt(action.metrics.fps)},")
                    appendLine("${action.metrics.sources["fps"]}")
                }
            }
        }
        
        outputFile.writeText(csv)
    }
    
    private fun formatMetricRow(name: String, stats: MetricStats): String {
        return "$name," +
            "${fmt(stats.min)}," +
            "${fmt(stats.max)}," +
            "${fmt(stats.avg)}," +
            "${fmt(stats.p50)}," +
            "${fmt(stats.p90)}," +
            "${fmt(stats.stddev)}," +
            "${stats.samples}"
    }
    
    private fun formatMetricValues(stats: MetricStats): String {
        return "${fmt(stats.min)},${fmt(stats.max)},${fmt(stats.avg)},${fmt(stats.p90)}"
    }
    
    /**
     * Export per-screen metrics to CSV.
     */
    fun exportScreenMetrics(
        screenMetrics: List<com.danioliveira.appium.metrics.android.ScreenMetrics>,
        outputFile: File
    ) {
        val csv = buildString {
            appendLine("Screen,Duration (ms),Start (ms),End (ms)," +
                "CPU Min (%),CPU Max (%),CPU Avg (%),CPU Samples," +
                "Memory Min (MB),Memory Max (MB),Memory Avg (MB),Memory Samples," +
                "FPS Min,FPS Max,FPS Avg,Frame Count,Jank Count,Jank %")
            
            screenMetrics.forEach { screen ->
                append("${screen.screenName},")
                append("${screen.durationMs},")
                append("${screen.startMs},")
                append("${screen.endMs},")
                append("${fmt(screen.cpuMin)},")
                append("${fmt(screen.cpuMax)},")
                append("${fmt(screen.cpuAvg)},")
                append("${screen.cpuSamples},")
                append("${screen.memoryMin},")
                append("${screen.memoryMax},")
                append("${screen.memoryAvg},")
                append("${screen.memorySamples},")
                append("${fmt(screen.fpsMin)},")
                append("${fmt(screen.fpsMax)},")
                append("${fmt(screen.fpsAvg)},")
                append("${screen.frameCount},")
                append("${screen.jankCount},")
                appendLine("${fmt(screen.jankPercentage)}")
            }
        }
        
        outputFile.writeText(csv)
    }
    
    /**
     * Export per-second FPS time series to CSV.
     */
    fun exportFpsTimeSeries(
        fpsPerSecond: List<com.danioliveira.appium.metrics.android.FpsPerSecond>,
        outputFile: File
    ) {
        val csv = buildString {
            appendLine("Second,FPS")
            
            fpsPerSecond.forEach { dataPoint ->
                appendLine("${dataPoint.second},${dataPoint.fps}")
            }
        }
        
        outputFile.writeText(csv)
    }
    
    private fun fmt(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
}


