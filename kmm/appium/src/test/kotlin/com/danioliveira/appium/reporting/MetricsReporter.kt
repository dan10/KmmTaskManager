package com.danioliveira.appium.reporting

import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.reporting.exporters.*
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MetricsReporter(private val outputDir: File) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    
    private val exporters = listOf(
        JsonExporter(),
        HtmlExporter(),
        CsvExporter(),
        MarkdownExporter()
    )
    
    init {
        outputDir.mkdirs()
    }
    
    fun generateReport(metricsManager: MetricsManager, testName: String = "metrics") {
        val timestamp = dateFormat.format(Date())
        val totalSummary = metricsManager.getTotalSummary()
        val filename = "$testName-$timestamp"
        
        exporters.forEach { exporter ->
            try {
                exporter.export(totalSummary, outputDir, filename)
            } catch (e: Exception) {
                logger.error("Failed to generate report with ${exporter.javaClass.simpleName}", e)
            }
        }
        
        logger.info("Generated metrics reports in ${outputDir.absolutePath}")
    }
}

