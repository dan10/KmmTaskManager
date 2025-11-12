package com.danioliveira.appium.reporting

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.slf4j.LoggerFactory
import java.io.File

class CsvReporter(private val outputDir: File) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val csvMapper = CsvMapper()
    
    fun writeRunResults(results: List<RunResult>) {
        val schema = CsvSchema.builder()
            .addColumn("cycle")
            .addColumn("duration_ms")
            .addColumn("jank_percentage")
            .addColumn("avg_frame_time_ms")
            .addColumn("rss_mb")
            .build()
        
        val csvFile = File(outputDir, "benchmark_results.csv")
        csvMapper.writer(schema).writeValues(csvFile).writeAll(
            results.map { result ->
                mapOf(
                    "cycle" to result.cycleNumber,
                    "duration_ms" to result.durationMs,
                    "jank_percentage" to result.jankPercentage,
                    "avg_frame_time_ms" to result.avgFrameTimeMs,
                    "rss_mb" to result.rssMb
                )
            }
        )
        
        logger.info("CSV report written to ${csvFile.absolutePath}")
    }
}

data class RunResult(
    val cycleNumber: Int,
    val durationMs: Long,
    val jankPercentage: Double,
    val avgFrameTimeMs: Double,
    val rssMb: Int
)


