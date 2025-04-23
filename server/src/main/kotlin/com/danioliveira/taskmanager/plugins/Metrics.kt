package com.danioliveira.taskmanager.plugins

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

/**
 * Configures metrics collection for the application using Micrometer and Prometheus.
 * Exposes metrics at the /metrics endpoint.
 */
fun Application.configureMetrics() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // Configure metrics collection
        meterBinders = listOf(
            io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics(),
            io.micrometer.core.instrument.binder.jvm.JvmGcMetrics(),
            io.micrometer.core.instrument.binder.system.ProcessorMetrics(),
            io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics()
        )
    }

    // Expose metrics endpoint
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}
