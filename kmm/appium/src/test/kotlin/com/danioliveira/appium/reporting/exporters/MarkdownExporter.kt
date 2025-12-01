package com.danioliveira.appium.reporting.exporters

import com.danioliveira.appium.metrics.TotalMetricsSummary
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class MarkdownExporter : ReportExporter {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun export(summary: TotalMetricsSummary, outputDir: File, filename: String) {
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
