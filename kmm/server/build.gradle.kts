import io.gatling.plugin.SimulationSelector.simulations

plugins {
    application
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.gatling)
}

group = "com.danioliveira.taskmanager"
version = "1.0.0"

application {
    mainClass.set("com.danioliveira.taskmanager.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xcontext-parameters", "-Xopt-in=kotlin.time.ExperimentalTime"))
    }
}

// Custom Gatling tasks for different scenarios
tasks.register("gatlingRunQuick") {
    group = "gatling"
    description = "Run Gatling load tests with reduced load for quick testing"
    dependsOn("gatlingRun")
    doFirst {
        System.setProperty("gatling.test.mode", "quick")
    }
}

tasks.register("gatlingRunLong") {
    group = "gatling"
    description = "Run Gatling load tests for 30 minutes with high load"
    dependsOn("gatlingRun")
    doFirst {
        System.setProperty("gatling.test.mode", "long")
    }
}

tasks.register("gatlingRunStress") {
    group = "gatling"
    description = "Run Gatling stress tests with very high load"
    dependsOn("gatlingRun")
    doFirst {
        System.setProperty("gatling.test.mode", "stress")
    }
}
dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    // Ktor Auth & JWT
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.r2dc)
    implementation(libs.exposed.kotlin.datetime)
    // PostgreSQL Driver (JDBC)
    implementation(libs.postgresql)
    implementation(libs.postgresql.r2dbc)
    // HikariCP for connection pooling
    implementation(libs.hikaricp)
    // Ktor Content Negotiation (for JSON)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    // Ktor Status Pages for exception handling
    implementation(libs.ktor.server.status.pages)
    // Ktor Request Validation
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.request.validation)
    // Google API Client for token verification
    implementation(libs.google.api)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    // Metrics and monitoring
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.koin.test)
    // H2 Database for testing
    testImplementation(libs.h2)
    // TestContainers for integration testing
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)

    // Load testing with Gatling
    // Required for Java DSL
    // Supporting Gatling modules (may or may not be needed depending on exact usage/reporting)
    gatlingImplementation(libs.gatling.core) // Keep scala core if other parts depend on it implicitly
    gatlingImplementation(libs.gatling.http) // Keep scala http if needed
    gatlingImplementation(libs.gatling.app)
    gatlingImplementation(libs.gatling.charts)
    gatlingImplementation(libs.ktor.serialization.kotlinx.json)
    gatlingImplementation(libs.kotlinx.datetime) // Add kotlinx-datetime for LocalDateTime support
    gatlingImplementation(projects.shared)
}