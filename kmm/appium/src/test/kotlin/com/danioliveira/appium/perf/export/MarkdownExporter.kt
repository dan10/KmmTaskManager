package com.danioliveira.appium.perf.export

import com.danioliveira.appium.perf.core.*
import java.io.File
import java.util.Locale

/**
 * Exports performance results to Markdown format.
 */
object MarkdownExporter {
    
    /**
     * Export a single performance result to Markdown.
     */
    fun exportPerformanceResult(result: PerformanceResult, outputFile: File) {
        val md = buildString {
            appendLine("# Performance Test Result")
            appendLine()
            appendLine("**Test:** ${result.testName}")
            appendLine("**Duration:** ${result.durationMs}ms")
            if (result.traceFile != null) {
                val traceType = when (result.traceFile.extension) {
                    "pftrace" -> "Perfetto"
                    "html" -> "Systrace"
                    else -> "Unknown"
                }
                appendLine("**Trace:** ${result.traceFile.name} ($traceType)")
            }
            appendLine()
            
            // Startup breakdown (if available)
            if (result.startupBreakdown != null) {
                appendLine("## App Startup")
                appendLine()
                appendLine("**Type:** ${result.startupBreakdown.type.replaceFirstChar { it.uppercase() }}")
                appendLine("**Total Duration:** ${fmt(result.startupBreakdown.totalMs)}ms")
                appendLine()
                
                if (result.startupBreakdown.phases.isNotEmpty()) {
                    appendLine("### Startup Phases")
                    appendLine()
                    appendLine("| Phase | Duration | % of Total |")
                    appendLine("|-------|----------|------------|")
                    
                    result.startupBreakdown.phases.forEach { phase ->
                        appendLine("| ${phase.name} | ${fmt(phase.durationMs)}ms | ${fmt(phase.percentageOfTotal)}% |")
                    }
                    
                    appendLine()
                }
            }
            
            appendLine("## Performance Metrics")
            appendLine()
            appendLine("| Metric | Min | Max | Avg | P50 | P90 | StdDev | Samples |")
            appendLine("|--------|-----|-----|-----|-----|-----|--------|---------|")
            appendLine(formatMetricRow("CPU (%)", result.metrics.cpu))
            appendLine(formatMetricRow("Memory (MB)", result.metrics.memory))
            appendLine(formatMetricRow("FPS", result.metrics.fps))
            appendLine()
            
            // Jank analysis (if available)
            if (result.jankStats != null) {
                appendLine("## Jank Analysis")
                appendLine()
                appendLine("**Total Frames:** ${result.jankStats.totalFrames}")
                appendLine("**Jank Frames:** ${result.jankStats.jankFrames} (${fmt(result.jankStats.jankPercentage)}%)")
                appendLine()
                
                appendLine("### Frame Timing")
                appendLine()
                appendLine("| Metric | Time (ms) |")
                appendLine("|--------|-----------|")
                appendLine("| Average | ${fmt(result.jankStats.avgFrameTimeMs)} |")
                appendLine("| Maximum | ${fmt(result.jankStats.maxFrameTimeMs)} |")
                appendLine("| P50 | ${fmt(result.jankStats.p50FrameTimeMs)} |")
                appendLine("| P90 | ${fmt(result.jankStats.p90FrameTimeMs)} |")
                appendLine("| P95 | ${fmt(result.jankStats.p95FrameTimeMs)} |")
                appendLine("| P99 | ${fmt(result.jankStats.p99FrameTimeMs)} |")
                appendLine()
                
                // Jank severity indicator
                val jankSeverity = when {
                    result.jankStats.jankPercentage < 1.0 -> "✅ Excellent"
                    result.jankStats.jankPercentage < 5.0 -> "✅ Good"
                    result.jankStats.jankPercentage < 10.0 -> "⚠️ Fair"
                    else -> "❌ Poor"
                }
                appendLine("**Jank Assessment:** $jankSeverity")
                appendLine()
            }
        }
        
        outputFile.writeText(md)
    }
    
    /**
     * Export scenario result to Markdown.
     */
    fun exportScenarioResult(result: ScenarioResult, outputFile: File) {
        val md = buildString {
            appendLine("# Scenario Performance Report")
            appendLine()
            appendLine("**Scenario:** ${result.scenarioName}")
            appendLine("**Iterations:** ${result.iterations}")
            appendLine()
            
            appendLine("## Aggregated Metrics (Across All Iterations)")
            appendLine()
            appendLine("| Metric | Mean of Means | Min of Mins | Max of Maxes | StdDev | 95% CI |")
            appendLine("|--------|---------------|-------------|--------------|--------|--------|")
            appendLine(formatAggregatedRow("CPU (%)", result.aggregatedMetrics.cpu))
            appendLine(formatAggregatedRow("Memory (MB)", result.aggregatedMetrics.memory))
            appendLine(formatAggregatedRow("FPS", result.aggregatedMetrics.fps))
            appendLine()
            
            appendLine("## Flow Results")
            appendLine()
            
            // Group by flow name
            val flowsByName = result.flowResults.groupBy { it.flowName }
            
            flowsByName.forEach { (flowName, iterations) ->
                appendLine("### $flowName")
                appendLine()
                appendLine("| Iteration | Duration (ms) | CPU Avg | Memory Avg | FPS Avg | Jank % |")
                appendLine("|-----------|---------------|---------|------------|---------|--------|")
                
                iterations.forEach { flow ->
                    val jankPct = flow.jankStats?.jankPercentage?.let { fmt(it) + "%" } ?: "N/A"
                    appendLine("| ${flow.iteration} | ${flow.durationMs} | " +
                        "${fmt(flow.metrics.cpu.avg)}% | " +
                        "${fmt(flow.metrics.memory.avg)}MB | " +
                        "${fmt(flow.metrics.fps.avg)} | " +
                        "$jankPct |")
                }
                
                appendLine()
                
                // Show jank analysis if available
                val flowsWithJank = iterations.filter { it.jankStats != null }
                if (flowsWithJank.isNotEmpty()) {
                    appendLine("#### Jank Analysis")
                    appendLine()
                    appendLine("| Iteration | Total Frames | Jank Frames | P50 (ms) | P90 (ms) | P99 (ms) |")
                    appendLine("|-----------|--------------|-------------|----------|----------|----------|")
                    
                    flowsWithJank.forEach { flow ->
                        val jank = flow.jankStats!!
                        appendLine("| ${flow.iteration} | ${jank.totalFrames} | ${jank.jankFrames} | " +
                            "${fmt(jank.p50FrameTimeMs)} | " +
                            "${fmt(jank.p90FrameTimeMs)} | " +
                            "${fmt(jank.p99FrameTimeMs)} |")
                    }
                    
                    appendLine()
                }
                
                appendLine()
                
                // Show screens if available
                if (iterations.first().screenResults.isNotEmpty()) {
                    appendLine("#### Screens")
                    appendLine()
                    appendLine("| Screen | Iteration | Duration (ms) | CPU Avg | Memory Avg | FPS Avg |")
                    appendLine("|--------|-----------|---------------|---------|------------|---------|")
                    
                    iterations.forEach { flow ->
                        flow.screenResults.forEach { screen ->
                            appendLine("| ${screen.screenName} | ${flow.iteration} | ${screen.durationMs} | " +
                                "${fmt(screen.metrics.cpu.avg)}% | " +
                                "${fmt(screen.metrics.memory.avg)}MB | " +
                                "${fmt(screen.metrics.fps.avg)} |")
                        }
                    }
                    
                    appendLine()
                }
            }
            
            appendLine("## Trace Files")
            appendLine()
            result.traceFiles.forEachIndexed { index, file ->
                appendLine("${index + 1}. `${file.name}`")
            }
        }
        
        outputFile.writeText(md)
    }
    
    private fun formatMetricRow(name: String, stats: MetricStats): String {
        return "| $name | ${fmt(stats.min)} | ${fmt(stats.max)} | ${fmt(stats.avg)} | " +
            "${fmt(stats.p50)} | ${fmt(stats.p90)} | ${fmt(stats.stddev)} | ${stats.samples} |"
    }
    
    private fun formatAggregatedRow(name: String, stats: AggregatedStats): String {
        val ci = "[${fmt(stats.confidenceInterval95.first)}, ${fmt(stats.confidenceInterval95.second)}]"
        return "| $name | ${fmt(stats.meanOfMeans)} | ${fmt(stats.minOfMins)} | " +
            "${fmt(stats.maxOfMaxes)} | ${fmt(stats.stddevAcrossIterations)} | $ci |"
    }
    
    /**
     * Export per-screen metrics to Markdown.
     */
    fun exportScreenMetrics(
        screenMetrics: List<com.danioliveira.appium.metrics.android.ScreenMetrics>,
        outputFile: File
    ) {
        val md = buildString {
            appendLine("# Per-Screen Performance Metrics")
            appendLine()
            
            screenMetrics.forEach { screen ->
                appendLine("## ${screen.screenName}")
                appendLine()
                appendLine("**Duration:** ${screen.durationMs}ms")
                appendLine("**Time Range:** ${screen.startMs}ms - ${screen.endMs}ms")
                appendLine()
                
                appendLine("### Metrics")
                appendLine()
                appendLine("| Metric | Min | Max | Avg | P50 | P90 | Samples |")
                appendLine("|--------|-----|-----|-----|-----|-----|---------|")
                appendLine("| CPU (%) | ${fmt(screen.cpuMin)} | ${fmt(screen.cpuMax)} | ${fmt(screen.cpuAvg)} | - | - | ${screen.cpuSamples} |")
                appendLine("| Memory (MB) | ${screen.memoryMin} | ${screen.memoryMax} | ${screen.memoryAvg} | ${screen.memoryP50} | ${screen.memoryP90} | ${screen.memorySamples} |")
                appendLine("| FPS | ${fmt(screen.fpsMin)} | ${fmt(screen.fpsMax)} | ${fmt(screen.fpsAvg)} | - | - | ${screen.frameCount} frames |")
                appendLine()
                
                if (screen.frameCount > 0) {
                    appendLine("### Frame Analysis")
                    appendLine()
                    appendLine("**Total Frames:** ${screen.frameCount}")
                    appendLine("**Jank Frames:** ${screen.jankCount} (${fmt(screen.jankPercentage)}%)")
                    appendLine()
                    
                    val jankSeverity = when {
                        screen.jankPercentage < 1.0 -> "✅ Excellent"
                        screen.jankPercentage < 5.0 -> "✅ Good"
                        screen.jankPercentage < 10.0 -> "⚠️ Fair"
                        else -> "❌ Poor"
                    }
                    appendLine("**Assessment:** $jankSeverity")
                    appendLine()
                }
                
                appendLine("---")
                appendLine()
            }
        }
        
        outputFile.writeText(md)
    }
    
    private fun fmt(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
    
    /**
     * Export aggregated result with histograms to Markdown.
     */
    fun exportAggregatedResultWithHistograms(result: AggregatedResultWithHistograms, outputFile: File) {
        val md = buildString {
            appendLine("# Aggregated Performance Results")
            appendLine()
            appendLine("**Total Runs:** ${result.totalRuns}")
            appendLine("**Successful Runs:** ${result.successfulRuns}")
            appendLine()
            
            appendLine("## Combined Statistics")
            appendLine()
            appendLine("Statistics computed from all merged samples across all runs.")
            appendLine()
            
            appendLine("| Metric | Min | Max | Avg | P50 | P90 | Samples |")
            appendLine("|--------|-----|-----|-----|-----|-----|---------|")
            appendLine("| CPU (%) | ${fmt(result.cpuStats.min)} | ${fmt(result.cpuStats.max)} | ${fmt(result.cpuStats.avg)} | ${fmt(result.cpuStats.p50)} | ${fmt(result.cpuStats.p90)} | ${result.cpuStats.samples} |")
            appendLine("| Memory (MB) | ${fmt(result.memoryStats.min)} | ${fmt(result.memoryStats.max)} | ${fmt(result.memoryStats.avg)} | ${fmt(result.memoryStats.p50)} | ${fmt(result.memoryStats.p90)} | ${result.memoryStats.samples} |")
            appendLine("| FPS | ${fmt(result.fpsStats.min)} | ${fmt(result.fpsStats.max)} | ${fmt(result.fpsStats.avg)} | ${fmt(result.fpsStats.p50)} | ${fmt(result.fpsStats.p90)} | ${result.fpsStats.samples} |")
            appendLine("| Duration (ms) | ${fmt(result.durationStats.min)} | ${fmt(result.durationStats.max)} | ${fmt(result.durationStats.avg)} | ${fmt(result.durationStats.p50)} | ${fmt(result.durationStats.p90)} | ${result.durationStats.samples} |")
            appendLine()
            
            appendLine("## Histograms")
            appendLine()
            
            appendLine("### CPU Distribution")
            appendLine()
            appendLine("```")
            appendLine(result.cpuHistogram.toAscii())
            appendLine("```")
            appendLine()
            
            appendLine("### Memory Distribution")
            appendLine()
            appendLine("```")
            appendLine(result.memoryHistogram.toAscii())
            appendLine("```")
            appendLine()
            
            appendLine("### FPS Distribution")
            appendLine()
            appendLine("```")
            appendLine(result.fpsHistogram.toAscii())
            appendLine("```")
            appendLine()
            
            appendLine("### Duration Distribution")
            appendLine()
            appendLine("```")
            appendLine(result.durationHistogram.toAscii())
            appendLine("```")
            appendLine()
            
            appendLine("## Individual Run Summaries")
            appendLine()
            appendLine("| Run | Duration (ms) | CPU Avg (%) | Memory Avg (MB) | FPS Avg |")
            appendLine("|-----|---------------|-------------|-----------------|---------|")
            result.individualRunSummaries.forEach { run ->
                appendLine("| ${run.runNumber} | ${run.durationMs} | ${fmt(run.cpuAvg)} | ${fmt(run.memoryAvg)} | ${fmt(run.fpsAvg)} |")
            }
            appendLine()
        }
        
        outputFile.writeText(md)
    }
}



