package com.danioliveira.appium.reporting.exporters

import com.danioliveira.appium.metrics.TotalMetricsSummary
import java.io.File

interface ReportExporter {
    fun export(summary: TotalMetricsSummary, outputDir: File, filename: String)
}
