import java.util.concurrent.TimeUnit

plugins {
    id("java-library")
    alias(libs.plugins.kotlinJvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // Appium
    testImplementation("io.appium:java-client:10.0.0")
    
    // Selenium
    testImplementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    
    // Jackson for JSON
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.2")
    
    // Gson for JSON (used by new perf framework)
    testImplementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines (used by new perf framework)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // AndroidX TraceProcessor (Perfetto query client)
    testImplementation("androidx.benchmark:benchmark-traceprocessor:1.4.1")
    
    // AndroidX Benchmark TraceProcessor (Perfetto SQL queries)
    // NOTE: These are AAR dependencies and require Android instrumentation test context
    // For now, we're using stub implementations that will be replaced when we integrate
    // the actual TraceProcessor HTTP server in Phase 3
    // testImplementation("androidx.benchmark:benchmark-common:1.3.3")
    // testImplementation("androidx.benchmark:benchmark-macro:1.3.3")
    
    // Logging
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    
    // Kotlin test
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

tasks.test {
    useJUnitPlatform()
    systemProperties(System.getProperties().mapKeys { it.key.toString() })
}

// Task to run iOS performance test with external Instruments recording
// This avoids the 3-minute timeout issue when using CpuAndGPU profile via Appium
tasks.register("iosPerformanceTestWithCpuGpu") {
    group = "verification"
    description = "Run iOS performance test with CpuAndGPU profile managed by Gradle"
    
    dependsOn("testClasses")
    
    doLast {
        val udid = System.getProperty("udid") ?: throw GradleException("UDID required: -Dudid=<device-id>")
        val bundleId = System.getProperty("bundleId") ?: "com.danioliveira.taskmanager.KmmTaskManager"
        val profile = System.getProperty("profile") ?: "Time Profiler"  // Default to Time Profiler
        val outputDir = file("build/ios-traces").apply { mkdirs() }
        val timestamp = System.currentTimeMillis()
        val traceFile = file("$outputDir/gradle_recording_$timestamp.trace")
        
        println("=".repeat(60))
        println("Starting Instruments Recording")
        println("Profile: $profile")
        println("Device: $udid")
        println("Bundle ID: $bundleId")
        println("Output: ${traceFile.absolutePath}")
        println("=".repeat(60))
        
        // Kill the app if it's running to ensure we capture full launch sequence
        println("Ensuring app is terminated before recording...")
        val killCmd = listOf(
            "sh", "-c",
            "xcrun devicectl device info processes --device $udid --quiet 2>/dev/null | grep '$bundleId' | awk '{print \$1}' | xargs -I {} xcrun devicectl device process signal --device $udid --signal 15 --pid {} 2>/dev/null || true"
        )
        
        val killProcess = ProcessBuilder(killCmd)
            .redirectErrorStream(true)
            .start()
        killProcess.waitFor()
        Thread.sleep(1000)  // Wait for app to fully terminate
        
        println("✅ App terminated. Recording will capture full launch sequence.")
        
        // Start xctrace recording with --launch to capture initialization
        // This will launch the app and record from the very beginning
        val recordCmd = listOf(
            "xcrun", "xctrace", "record",
            "--device", udid,
            "--template", "CpuAndGPU",
            "--output", traceFile.absolutePath,
            "--launch", bundleId,
            "--time-limit", "5m"  // 5 minute max
        )
        
        println("\nStarting recording...")
        val recordProcess = ProcessBuilder(recordCmd)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        
        // Monitor output in background thread
        Thread {
            recordProcess.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        println("[xctrace] $line")
                    }
                }
            }
        }.start()
        
        // Wait for recording to start
        Thread.sleep(5000)
        println("✅ Recording started\n")
        
        // Run the Appium test WITHOUT internal recording
        println("=".repeat(60))
        println("Running Appium Test")
        println("=".repeat(60))
        
        val testExitCode = try {
            val testProcess = ProcessBuilder(
                "../gradlew", "test",
                "--tests", "PerformanceMetricsTest.testLoginFlowWithMetrics",
                "-Dudid=$udid",
                "-DbundleId=$bundleId",
                "-DskipInstrumentsRecording=true",  // Flag to skip internal recording
                "--no-configuration-cache"
            )
                .directory(projectDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .apply {
                    environment()["PLATFORM"] = "IOS"  // Set platform for test
                }
                .start()
            
            testProcess.waitFor()
        } catch (e: Exception) {
            println("⚠️  Test execution error: ${e.message}")
            1
        }
        
        if (testExitCode == 0) {
            println("✅ Test completed successfully")
        } else {
            println("⚠️  Test failed with exit code: $testExitCode")
        }
        
        // Wait a bit for test to complete
        Thread.sleep(2000)
        
        // Stop recording (Ctrl+C)
        println("\n" + "=".repeat(60))
        println("Stopping recording...")
        println("=".repeat(60))
        recordProcess.destroy()
        
        // Wait for process to finish and trace to be written
        val stopped = recordProcess.waitFor(45, TimeUnit.SECONDS)
        if (!stopped) {
            println("⚠️  Recording didn't stop gracefully, forcing...")
            recordProcess.destroyForcibly()
            recordProcess.waitFor()
        }
        
        // Wait for trace file to be finalized
        Thread.sleep(5000)
        
        println()
        if (traceFile.exists()) {
            // Calculate size using du (trace files are directories)
            val duProcess = ProcessBuilder("du", "-sh", traceFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            val sizeOutput = duProcess.inputStream.bufferedReader().readText().trim()
            duProcess.waitFor()
            val sizeString = sizeOutput.split("\\s+".toRegex()).firstOrNull() ?: "unknown"
            
            println("✅ Trace saved: ${traceFile.absolutePath}")
            println("📊 Size: $sizeString")
            println("\n🔍 To view in Instruments:")
            println("   open \"${traceFile.absolutePath}\"")
            println("\n💡 Analyze GPU metrics, Metal performance, Core Animation, and more!")
        } else {
            println("❌ Trace file not found at: ${traceFile.absolutePath}")
            println("   Check xctrace output above for errors")
        }
        
        println("\n" + "=".repeat(60))
        println("Performance profiling completed")
        println("=".repeat(60))
    }
}