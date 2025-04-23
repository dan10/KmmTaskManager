plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.graalvmNative)
    application
}

group = "com.danioliveira.taskmanager"
version = "1.0.0"

application {
    mainClass.set("com.danioliveira.taskmanager.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

// GraalVM Native Image configuration
graalvmNative {
    binaries {
        named("main") {
            imageName.set("task-manager-native")
            mainClass.set("com.danioliveira.taskmanager.ApplicationKt")
            debug.set(false)
            verbose.set(true)
            fallback.set(false)
            buildArgs.add("--no-fallback")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("-H:+PrintClassInitialization")
        }
    }
}

// Custom tasks for running in different modes
tasks.register<JavaExec>("runJvm") {
    group = "application"
    description = "Runs the application in JVM mode"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.danioliveira.taskmanager.ApplicationKt")
    jvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

tasks.register<Exec>("runNative") {
    group = "application"
    description = "Runs the application in GraalVM native mode"
    dependsOn("nativeCompile")

    val nativeImagePath = "${buildDir}/native/nativeCompile/task-manager-native"
    commandLine(nativeImagePath)
}

// Task for running Gatling load test for 30 minutes
tasks.register("gatlingRunLong") {
    group = "load testing"
    description = "Runs Gatling load tests for 30 minutes"
    dependsOn("gatlingRun-com.danioliveira.taskmanager.loadtest.TaskApiSimulation")
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
    testImplementation(libs.gatling.core)
    testImplementation(libs.gatling.http)
    testImplementation(libs.gatling.app)
    testImplementation(libs.gatling.charts)
}
