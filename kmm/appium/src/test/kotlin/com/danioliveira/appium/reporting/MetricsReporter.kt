package com.danioliveira.appium.reporting

import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.metrics.PageMetricsSummary
import com.danioliveira.appium.metrics.TotalMetricsSummary
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MetricsReporter(private val outputDir: File) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)
    
    init {
        outputDir.mkdirs()
    }
    
    fun generateReport(metricsManager: MetricsManager, testName: String = "metrics") {
        val timestamp = dateFormat.format(Date())
        val totalSummary = metricsManager.getTotalSummary()
        
        // Generate all report formats
        generateJsonReport(totalSummary, "$testName-$timestamp")
        generateHtmlReport(totalSummary, "$testName-$timestamp")
        generateCsvReport(totalSummary, "$testName-$timestamp")
        generateMarkdownReport(totalSummary, "$testName-$timestamp")
        
        logger.info("Generated metrics reports in ${outputDir.absolutePath}")
    }
    
    private fun generateJsonReport(summary: TotalMetricsSummary, filename: String) {
        val jsonFile = File(outputDir, "$filename.json")
        objectMapper.writeValue(jsonFile, summary)
        logger.info("JSON report: ${jsonFile.absolutePath}")
    }
    
    private fun generateHtmlReport(summary: TotalMetricsSummary, filename: String) {
        val htmlFile = File(outputDir, "$filename.html")
        
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head>")
            appendLine("<title>Performance Metrics Report - ${summary.platform}</title>")
            appendLine("""
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
                    .container { max-width: 1400px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; }
                    h1 { color: #333; border-bottom: 3px solid #4CAF50; padding-bottom: 10px; }
                    h2 { color: #555; margin-top: 30px; border-bottom: 2px solid #2196F3; padding-bottom: 8px; }
                    h3 { color: #666; margin-top: 20px; }
                    .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }
                    .metric-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .metric-card.memory { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
                    .metric-card.cpu { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
                    .metric-card.render { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
                    .metric-card h4 { margin: 0 0 10px 0; font-size: 14px; opacity: 0.9; }
                    .metric-card .value { font-size: 32px; font-weight: bold; margin: 10px 0; }
                    .metric-card .unit { font-size: 16px; opacity: 0.8; }
                    table { width: 100%; border-collapse: collapse; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    th { background: #4CAF50; color: white; padding: 12px; text-align: left; font-weight: 600; }
                    td { padding: 12px; border-bottom: 1px solid #ddd; }
                    tr:hover { background: #f5f5f5; }
                    .good { color: #4CAF50; font-weight: bold; }
                    .warning { color: #ff9800; font-weight: bold; }
                    .bad { color: #f44336; font-weight: bold; }
                    .page-section { margin: 30px 0; padding: 20px; background: #fafafa; border-radius: 8px; }
                </style>
            """.trimIndent())
            appendLine("</head><body>")
            appendLine("<div class='container'>")
            
            // Header
            appendLine("<h1>📊 Performance Metrics Report</h1>")
            appendLine("<p><strong>Platform:</strong> ${summary.platform}</p>")
            appendLine("<p><strong>Total Pages:</strong> ${summary.pageCount}</p>")
            appendLine("<p><strong>Total Actions:</strong> ${summary.totalActions}</p>")
            
            // Overall Summary Cards
            appendLine("<h2>📈 Overall Summary</h2>")
            appendLine("<div class='summary-grid'>")
            
            appendLine("""
                <div class='metric-card'>
                    <h4>🚀 App Launch Time</h4>
                    <div class='value'>${summary.appLaunchTimeMs}</div>
                    <div class='unit'>milliseconds</div>
                </div>
            """.trimIndent())
            
            appendLine("""
                <div class='metric-card memory'>
                    <h4>💾 Memory Usage</h4>
                    <div class='value'>${String.format("%.1f", summary.avgMemoryMb)}</div>
                    <div class='unit'>MB avg (Peak: ${summary.peakMemoryMb} MB)</div>
                </div>
            """.trimIndent())
            
            appendLine("""
                <div class='metric-card cpu'>
                    <h4>⚡ CPU Usage</h4>
                    <div class='value'>${String.format("%.1f", summary.avgCpuPercent)}</div>
                    <div class='unit'>% avg (Peak: ${String.format("%.1f", summary.peakCpuPercent)}%)</div>
                </div>
            """.trimIndent())
            
            appendLine("""
                <div class='metric-card render'>
                    <h4>🎨 Rendering</h4>
                    <div class='value'>${String.format("%.1f", summary.avgFps)}</div>
                    <div class='unit'>FPS (${String.format("%.2f", summary.avgFrameTimeMs)}ms/frame)</div>
                </div>
            """.trimIndent())
            
            appendLine("</div>")
            
            // Per-Page Summaries
            appendLine("<h2>📄 Per-Page Metrics</h2>")
            
            for (page in summary.pageSummaries.sortedBy { it.pageName }) {
                appendLine("<div class='page-section'>")
                appendLine("<h3>📱 ${page.pageName} Page</h3>")
                appendLine("<p><strong>Actions:</strong> ${page.actionCount} | " +
                    "<strong>Total Time:</strong> ${page.totalDurationMs}ms | " +
                    "<strong>Avg Time:</strong> ${String.format("%.1f", page.avgDurationMs)}ms</p>")
                
                appendLine("<table>")
                appendLine("<tr>")
                appendLine("<th>Action</th>")
                appendLine("<th>Duration (ms)</th>")
                appendLine("<th>Memory (MB)</th>")
                appendLine("<th>CPU %</th>")
                appendLine("<th>Frame Time (ms)</th>")
                appendLine("<th>FPS</th>")
                if (summary.platform == "ANDROID") {
                    appendLine("<th>Jank %</th>")
                }
                appendLine("</tr>")
                
                for (action in page.actions) {
                    appendLine("<tr>")
                    appendLine("<td>${action.actionName}</td>")
                    appendLine("<td>${action.durationMs}</td>")
                    appendLine("<td>${action.memoryMb}</td>")
                    appendLine("<td>${String.format("%.1f", action.cpuPercent)}</td>")
                    
                    val frameTimeClass = when {
                        action.avgFrameTimeMs < 16.67 -> "good"
                        action.avgFrameTimeMs < 33.0 -> "warning"
                        else -> "bad"
                    }
                    appendLine("<td class='$frameTimeClass'>${String.format("%.2f", action.avgFrameTimeMs)}</td>")
                    
                    val fpsClass = when {
                        action.fps >= 55 -> "good"
                        action.fps >= 30 -> "warning"
                        else -> "bad"
                    }
                    appendLine("<td class='$fpsClass'>${String.format("%.1f", action.fps)}</td>")
                    
                    if (summary.platform == "ANDROID") {
                        val jankClass = when {
                            action.jankPercentage < 5 -> "good"
                            action.jankPercentage < 15 -> "warning"
                            else -> "bad"
                        }
                        appendLine("<td class='$jankClass'>${String.format("%.1f", action.jankPercentage)}</td>")
                    }
                    appendLine("</tr>")
                }
                
                appendLine("</table>")
                appendLine("</div>")
            }
            
            appendLine("</div>")
            appendLine("</body></html>")
        }
        
        htmlFile.writeText(html)
        logger.info("HTML report: ${htmlFile.absolutePath}")
    }
    
    private fun generateCsvReport(summary: TotalMetricsSummary, filename: String) {
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
    
    private fun generateMarkdownReport(summary: TotalMetricsSummary, filename: String) {
        val mdFile = File(outputDir, "$filename.md")
        
        val markdown = buildString {
            appendLine("# Performance Metrics Report")
            appendLine()
            appendLine("**Platform:** ${summary.platform}")
            appendLine()
            appendLine("**Generated:** ${Date()}")
            appendLine()
            
            appendLine("## Overall Summary")
            appendLine()
            appendLine("| Metric | Value |")
            appendLine("|--------|-------|")
            appendLine("| 🚀 App Launch Time | ${summary.appLaunchTimeMs} ms |")
            appendLine("| 📊 Total Actions | ${summary.totalActions} |")
            appendLine("| 📄 Total Pages | ${summary.pageCount} |")
            appendLine("| ⏱️ Avg Action Duration | ${String.format("%.1f", summary.avgActionDurationMs)} ms |")
            appendLine("| 💾 Avg Memory | ${String.format("%.1f", summary.avgMemoryMb)} MB |")
            appendLine("| 💾 Peak Memory | ${summary.peakMemoryMb} MB |")
            appendLine("| ⚡ Avg CPU | ${String.format("%.1f", summary.avgCpuPercent)}% |")
            appendLine("| ⚡ Peak CPU | ${String.format("%.1f", summary.peakCpuPercent)}% |")
            appendLine("| 🎨 Avg FPS | ${String.format("%.1f", summary.avgFps)} |")
            appendLine("| 🎨 Avg Frame Time | ${String.format("%.2f", summary.avgFrameTimeMs)} ms |")
            if (summary.platform == "ANDROID") {
                appendLine("| 🎬 Avg Jank | ${String.format("%.1f", summary.avgJankPercentage)}% |")
            }
            appendLine()
            
            appendLine("## Per-Page Metrics")
            appendLine()
            
            for (page in summary.pageSummaries.sortedBy { it.pageName }) {
                appendLine("### ${page.pageName} Page")
                appendLine()
                appendLine("**Actions:** ${page.actionCount} | " +
                    "**Total Time:** ${page.totalDurationMs}ms | " +
                    "**Avg Time:** ${String.format("%.1f", page.avgDurationMs)}ms")
                appendLine()
                
                appendLine("| Action | Duration (ms) | Memory (MB) | CPU % | Frame Time (ms) | FPS |")
                appendLine("|--------|---------------|-------------|-------|-----------------|-----|")
                
                for (action in page.actions) {
                    append("| ${action.actionName} ")
                    append("| ${action.durationMs} ")
                    append("| ${action.memoryMb} ")
                    append("| ${String.format("%.1f", action.cpuPercent)} ")
                    append("| ${String.format("%.2f", action.avgFrameTimeMs)} ")
                    append("| ${String.format("%.1f", action.fps)} |")
                    appendLine()
                }
                appendLine()
            }
        }
        
        mdFile.writeText(markdown)
        logger.info("Markdown report: ${mdFile.absolutePath}")
    }
}

