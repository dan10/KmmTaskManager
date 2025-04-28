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
    implementation(libs.exposed.java.time)
    // PostgreSQL Driver
    implementation(libs.postgresql)
    // Ktor Content Negotiation (for JSON)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    // Ktor Status Pages for exception handling
    implementation(libs.ktor.server.status.pages)
    // Ktor Request Validation
    implementation(libs.ktor.server.request.validation)
    // Ktor File Uploads
    implementation(libs.ktor.server.cio)
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

    // Load testing with Gatling
    // Required for Java DSL
    // Supporting Gatling modules (may or may not be needed depending on exact usage/reporting)
    gatlingImplementation(libs.gatling.core) // Keep scala core if other parts depend on it implicitly
    gatlingImplementation(libs.gatling.http) // Keep scala http if needed
    gatlingImplementation(libs.gatling.app)
    gatlingImplementation(libs.gatling.charts)
    gatlingImplementation(libs.ktor.serialization.kotlinx.json)
    gatlingImplementation(projects.shared)
}
