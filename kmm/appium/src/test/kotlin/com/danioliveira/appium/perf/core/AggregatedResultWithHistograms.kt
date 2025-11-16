package com.danioliveira.appium.perf.core

import com.danioliveira.appium.metrics.android.perfetto.PerfettoMetricsExtractor
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Aggregated result with histogram distributions from merged samples.
 * 
 * This class merges all raw samples from multiple PerformanceResults and computes
 * combined statistics and histograms.
 */
data class AggregatedResultWithHistograms(
    val totalRuns: Int,
    val successfulRuns: Int,
    val cpuStats: MetricStats,
    val memoryStats: MetricStats,
    val fpsStats: MetricStats,
    val durationStats: MetricStats,
    val cpuHistogram: Histogram,
    val memoryHistogram: Histogram,
    val fpsHistogram: Histogram,
    val durationHistogram: Histogram,
    val individualRunSummaries: List<RunSummary>
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AggregatedResultWithHistograms::class.java)
        
        /**
         * Aggregate multiple PerformanceResults by merging all raw samples.
         * 
         * @param results List of PerformanceResults from multiple runs
         * @param packageName Package name for extracting trace data
         * @return Aggregated result with histograms
         */
        fun from(results: List<PerformanceResult>, packageName: String): AggregatedResultWithHistograms {
            logger.info("Aggregating ${results.size} results...")
            
            // Collect all raw samples
            val allCpuSamples = mutableListOf<Double>()
            val allMemorySamples = mutableListOf<Double>()
            val allFpsSamples = mutableListOf<Double>()
            val allDurationSamples = mutableListOf<Double>()
            
            val runSummaries = mutableListOf<RunSummary>()
            
            results.forEachIndexed { index, result ->
                logger.info("Processing run ${index + 1}/${results.size}...")
                
                // Add duration
                allDurationSamples.add(result.durationMs.toDouble())
                
                // Extract raw samples from trace file if available
                if (result.traceFile != null && result.traceFile.exists()) {
                    try {
                        PerfettoMetricsExtractor(result.traceFile).use { extractor ->
                            // Extract CPU samples
                            val cpuData = extractor.extractCpuUtilization(packageName)
                            if (cpuData.isNotEmpty()) {
                                // Bucket CPU into percentages per time window
                                val cpuPercentages = bucketizeCpuToPercentages(cpuData)
                                allCpuSamples.addAll(cpuPercentages)
                                logger.info("  CPU: ${cpuPercentages.size} samples")
                            }
                            
                            // Extract memory samples
                            val memoryData = extractor.extractMemoryUsage(packageName)
                            if (memoryData.isNotEmpty()) {
                                val memoryMb = memoryData.map { it.rssMb.toDouble() }
                                allMemorySamples.addAll(memoryMb)
                                logger.info("  Memory: ${memoryMb.size} samples")
                            }
                            
                            // Extract FPS samples
                            val fpsData = extractor.extractFpsPerSecond(packageName)
                            if (fpsData.isNotEmpty()) {
                                val fpsSamples = fpsData.map { it.second.toDouble() }
                                allFpsSamples.addAll(fpsSamples)
                                logger.info("  FPS: ${fpsSamples.size} samples")
                            }
                        }
                    } catch (e: Exception) {
                        logger.warn("Failed to extract samples from trace ${result.traceFile.name}: ${e.message}")
                    }
                }
                
                // Create run summary
                runSummaries.add(
                    RunSummary(
                        runNumber = index + 1,
                        durationMs = result.durationMs,
                        cpuAvg = result.metrics.cpu.avg,
                        memoryAvg = result.metrics.memory.avg,
                        fpsAvg = result.metrics.fps.avg
                    )
                )
            }
            
            logger.info("Total samples collected:")
            logger.info("  CPU: ${allCpuSamples.size}")
            logger.info("  Memory: ${allMemorySamples.size}")
            logger.info("  FPS: ${allFpsSamples.size}")
            logger.info("  Duration: ${allDurationSamples.size}")
            
            // Compute combined statistics
            val cpuStats = MetricStats.from(allCpuSamples)
            val memoryStats = MetricStats.from(allMemorySamples)
            val fpsStats = MetricStats.from(allFpsSamples)
            val durationStats = MetricStats.from(allDurationSamples)
            
            // Build histograms
            val cpuHistogram = Histogram.from(allCpuSamples, binWidth = 5.0, unit = "%")
            val memoryHistogram = Histogram.from(allMemorySamples, binWidth = 10.0, unit = "MB")
            val fpsHistogram = Histogram.from(allFpsSamples, binWidth = 5.0, unit = "fps")
            val durationHistogram = Histogram.from(allDurationSamples, binWidth = 1000.0, unit = "ms")
            
            return AggregatedResultWithHistograms(
                totalRuns = results.size,
                successfulRuns = results.size,
                cpuStats = cpuStats,
                memoryStats = memoryStats,
                fpsStats = fpsStats,
                durationStats = durationStats,
                cpuHistogram = cpuHistogram,
                memoryHistogram = memoryHistogram,
                fpsHistogram = fpsHistogram,
                durationHistogram = durationHistogram,
                individualRunSummaries = runSummaries
            )
        }
        
        /**
         * Bucketize CPU scheduling slices into time windows and calculate CPU percentage per window.
         */
        private fun bucketizeCpuToPercentages(
            cpu: List<com.danioliveira.appium.metrics.android.perfetto.CpuUtilization>
        ): List<Double> {
            if (cpu.isEmpty()) return emptyList()
            
            val windowSizeMs = 100L // 100ms windows
            val windowSizeNs = windowSizeMs * 1_000_000
            
            val traceStartNs = cpu.minOf { it.timestampNs }
            val traceEndNs = cpu.maxOf { it.timestampNs + it.durationNs }
            
            // Group slices by time window
            val windowMap = mutableMapOf<Long, Long>() // window -> total CPU time in ns
            
            cpu.forEach { slice ->
                val windowIndex = (slice.timestampNs - traceStartNs) / windowSizeNs
                val cpuTimeInWindow = slice.durationNs
                windowMap[windowIndex] = (windowMap[windowIndex] ?: 0) + cpuTimeInWindow
            }
            
            // Convert to percentages
            return windowMap.values.map { cpuTimeNs ->
                val percentage = (cpuTimeNs.toDouble() / windowSizeNs) * 100.0
                percentage.coerceIn(0.0, 100.0)
            }
        }
    }
    
    /**
     * Export to JSON file.
     */
    fun writeJson(outputFile: File) {
        com.danioliveira.appium.perf.export.JsonExporter.exportAggregatedResultWithHistograms(this, outputFile)
    }
    
    /**
     * Export to Markdown file.
     */
    fun writeMarkdown(outputFile: File) {
        com.danioliveira.appium.perf.export.MarkdownExporter.exportAggregatedResultWithHistograms(this, outputFile)
    }
    
    /**
     * Export all formats to directory.
     */
    fun writeAll(outputDir: File) {
        outputDir.mkdirs()
        
        writeJson(File(outputDir, "aggregated_results.json"))
        writeMarkdown(File(outputDir, "aggregated_results.md"))
        
        // Export individual run summaries CSV
        val csvFile = File(outputDir, "run_summaries.csv")
        com.danioliveira.appium.perf.export.CsvExporter.exportRunSummaries(individualRunSummaries, csvFile)
    }
}

/**
 * Summary of a single run.
 */
data class RunSummary(
    val runNumber: Int,
    val durationMs: Long,
    val cpuAvg: Double,
    val memoryAvg: Double,
    val fpsAvg: Double
)

/**
 * Histogram distribution of metric values.
 */
data class Histogram(
    val bins: List<HistogramBin>,
    val binWidth: Double,
    val unit: String,
    val totalSamples: Int
) {
    companion object {
        /**
         * Create histogram from samples.
         * 
         * @param samples List of sample values
         * @param binWidth Width of each bin
         * @param unit Unit of measurement
         * @return Histogram
         */
        fun from(samples: List<Double>, binWidth: Double, unit: String): Histogram {
            if (samples.isEmpty()) {
                return Histogram(emptyList(), binWidth, unit, 0)
            }
            
            val min = samples.minOrNull() ?: 0.0
            val max = samples.maxOrNull() ?: 0.0
            
            // Calculate bin ranges
            val minBin = floor(min / binWidth) * binWidth
            val maxBin = ceil(max / binWidth) * binWidth
            
            // Create bins
            val binMap = mutableMapOf<Double, Int>()
            var currentBin = minBin
            while (currentBin <= maxBin) {
                binMap[currentBin] = 0
                currentBin += binWidth
            }
            
            // Count samples in each bin
            samples.forEach { value ->
                val binStart = floor(value / binWidth) * binWidth
                binMap[binStart] = (binMap[binStart] ?: 0) + 1
            }
            
            // Convert to list of bins
            val bins = binMap.entries
                .sortedBy { it.key }
                .map { (start, count) ->
                    HistogramBin(
                        rangeStart = start,
                        rangeEnd = start + binWidth,
                        count = count,
                        percentage = (count.toDouble() / samples.size) * 100.0
                    )
                }
                .filter { it.count > 0 } // Only include non-empty bins
            
            return Histogram(bins, binWidth, unit, samples.size)
        }
    }
    
    /**
     * Generate ASCII histogram for display.
     */
    fun toAscii(maxBarLength: Int = 50): String {
        if (bins.isEmpty()) return "No data"
        
        val maxCount = bins.maxOf { it.count }
        val scale = maxBarLength.toDouble() / maxCount
        
        return buildString {
            bins.forEach { bin ->
                val barLength = (bin.count * scale).toInt()
                val bar = "█".repeat(barLength)
                val range = String.format("%.1f-%.1f %s", bin.rangeStart, bin.rangeEnd, unit)
                appendLine(String.format("%-20s | %-50s %5d (%.1f%%)", range, bar, bin.count, bin.percentage))
            }
        }
    }
}

/**
 * A single bin in a histogram.
 */
data class HistogramBin(
    val rangeStart: Double,
    val rangeEnd: Double,
    val count: Int,
    val percentage: Double
)


