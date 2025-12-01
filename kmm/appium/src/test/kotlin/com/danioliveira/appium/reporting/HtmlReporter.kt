package com.danioliveira.appium.reporting

import org.slf4j.LoggerFactory
import java.io.File

class HtmlReporter(private val outputDir: File) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun generateReport(results: List<RunResult>, config: ReportConfig) {
        val htmlFile = File(outputDir, "benchmark_report.html")
        
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><title>Benchmark Report</title></head><body>")
            appendLine("<h1>Performance Benchmark Report</h1>")
            appendLine("<h2>${config.platform} - ${config.app}</h2>")
            appendLine("<table border='1'>")
            appendLine("<tr><th>Cycle</th><th>Duration (ms)</th><th>Jank %</th><th>Avg Frame Time (ms)</th><th>RSS (MB)</th></tr>")
            
            results.forEach { result ->
                appendLine("<tr>")
                appendLine("<td>${result.cycleNumber}</td>")
                appendLine("<td>${result.durationMs}</td>")
                appendLine("<td>${String.format("%.2f", result.jankPercentage)}</td>")
                appendLine("<td>${String.format("%.2f", result.avgFrameTimeMs)}</td>")
                appendLine("<td>${result.rssMb}</td>")
                appendLine("</tr>")
            }
            
            appendLine("</table>")
            appendLine("</body></html>")
        }
        
        htmlFile.writeText(html)
        logger.info("HTML report written to ${htmlFile.absolutePath}")
    }
}

data class ReportConfig(
    val platform: String,
    val app: String,
    val runs: Int
)


