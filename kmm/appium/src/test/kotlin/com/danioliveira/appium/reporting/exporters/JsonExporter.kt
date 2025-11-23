package com.danioliveira.appium.reporting.exporters

import com.danioliveira.appium.metrics.TotalMetricsSummary
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.io.File

class JsonExporter : ReportExporter {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    override fun export(summary: TotalMetricsSummary, outputDir: File, filename: String) {
        val jsonFile = File(outputDir, "$filename.json")
        objectMapper.writeValue(jsonFile, summary)
        logger.info("JSON report: ${jsonFile.absolutePath}")
    }
}
