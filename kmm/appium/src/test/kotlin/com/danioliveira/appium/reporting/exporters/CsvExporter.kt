package com.danioliveira.appium.reporting.exporters

import com.danioliveira.appium.metrics.TotalMetricsSummary
import org.slf4j.LoggerFactory
import java.io.File

class CsvExporter : ReportExporter {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun export(summary: TotalMetricsSummary, outputDir: File, filename: String) {
        val csvFile = File(outputDir, "$filename.csv")
        
        val csv = buildString {
            // Header
            appendLine("Page,Action,Duration(ms),Memory(MB),CPU%,FrameTime(ms),FPS,Jank%,Platform")
            
            // Data rows
            for (page in summary.pageSummaries) {
                for (action in page.actions) {
                    appendLine("${action.pageName},${action.actionName},${action.durationMs}," +
                        "${action.memoryMb},${String.format("%.2f", action.cpuPercent)}," +
                        "${String.format("%.2f", action.avgFrameTimeMs)}," +
                        "${String.format("%.2f", action.fps)}," +
                        "${String.format("%.2f", action.jankPercentage)}," +
                        "${action.platform}")
                }
            }
        }
        
        csvFile.writeText(csv)
        logger.info("CSV report: ${csvFile.absolutePath}")
    }
}
