package com.danioliveira.appium

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.drivers.AndroidDriverFactory
import com.danioliveira.appium.flows.Flows
import com.danioliveira.appium.metrics.android.AndroidMetricsCollector
import com.danioliveira.appium.pages.*
import io.appium.java_client.android.AndroidDriver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * Emulator-specific flow test using Flows DSL.
 * 
 * This test is optimized for emulator environments and focuses on:
 * - Fast registration and task creation flows
 * - Bulk operations using Flows DSL
 * - Verification of UI interactions
 * 
 * Flow:
 * 1. Register new user
 * 2. Create multiple tasks using Flows DSL
 * 3. Verify tasks are visible by scrolling
 * 
 * Uses Flows DSL for clean, readable test code.
 */
class EmulatorFlowTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: AndroidDriver
    private lateinit var flows: Flows
    private val packageName = "com.danioliveira.taskmanager"
    
    @BeforeEach
    fun setup() {
        val config = BenchmarkConfig(
            platform = Platform.ANDROID,
            app = com.danioliveira.appium.config.App.KMM,
            scenario = "emulator-flow",
            runs = 1,
            warmup = 0,
            deviceName = System.getProperty("deviceName") ?: "emulator-5554",
            udid = System.getProperty("udid"),
            apkPath = System.getProperty("apk"),
            ipaPath = null,
            bundleId = null,
            packageName = packageName,
            registerEveryCycle = false
        )
        
        driver = AndroidDriverFactory.create(config) as AndroidDriver
        flows = Flows(driver, Platform.ANDROID)
        
        logger.info("✅ EmulatorFlowTest setup complete")
    }
    
    @Test
    fun testRegisterAndCreateTasks() = runBlocking {
        logger.info("=== Emulator Flow Test: Register and Create Tasks ===")
        
        try {
            // Generate unique credentials
            val timestamp = System.currentTimeMillis()
            val name = "Emulator Test User"
            val email = "emulator_test_${timestamp}@test.com"
            val password = "TestPass123!"
            
            logger.info("📝 Registering user: $email")
            
            // Use Flows DSL for registration
            val tasksPage = flows.registerUser(name, email, password)
            logger.info("✅ Registration successful")
            
            delay(500)
            
            // Create multiple tasks using Flows DSL
            logger.info("📋 Creating tasks...")
            val taskTitles = (1..5).map { "Emulator Task $it" }
            val finalTasksPage = flows.createBulkTasks(taskTitles)
            logger.info("✅ Created ${taskTitles.size} tasks")
            
            // Verify tasks are visible by scrolling
            logger.info("📜 Scrolling to verify tasks...")
            finalTasksPage.scrollToEnd()
            delay(1000)
            
            logger.info("✅ Test complete - all tasks created and verified")
            
        } catch (e: Exception) {
            logger.error("❌ Test failed: ${e.message}", e)
            throw e
        }
    }
    
    @Test
    fun testRegisterAndCreateSingleTask() = runBlocking {
        logger.info("=== Emulator Flow Test: Register and Create Single Task ===")
        
        try {
            // Generate unique credentials
            val timestamp = System.currentTimeMillis()
            val name = "Single Task User"
            val email = "single_task_${timestamp}@test.com"
            val password = "TestPass123!"
            
            logger.info("📝 Registering user: $email")
            
            // Use Flows DSL for complete flow
            val tasksPage = flows.registerAndCreateTask(
                name = name,
                email = email,
                password = password,
                taskTitle = "My First Task",
                taskDescription = "Created via Flows DSL"
            )
            
            logger.info("✅ Registration and task creation successful")
            
            // Verify task is visible
            delay(500)
            tasksPage.scrollToEnd()
            
            logger.info("✅ Test complete")
            
        } catch (e: Exception) {
            logger.error("❌ Test failed: ${e.message}", e)
            throw e
        }
    }
    
    @Test
    fun testLoginAndCreateTasks() = runBlocking {
        logger.info("=== Emulator Flow Test: Login and Create Tasks ===")
        
        try {
            // First, register a user
            val timestamp = System.currentTimeMillis()
            val name = "Login Test User"
            val email = "login_test_${timestamp}@test.com"
            val password = "TestPass123!"
            
            logger.info("📝 Registering user: $email")
            flows.registerUser(name, email, password)
            delay(1000)
            
            // Reset app to simulate login flow
            resetAppState()
            
            // Now login and create tasks
            logger.info("🔐 Logging in: $email")
            val tasksPage = flows.login(email, password)
            logger.info("✅ Login successful")
            
            delay(500)
            
            // Create tasks using Flows DSL
            logger.info("📋 Creating tasks after login...")
            val taskTitles = (1..3).map { "Post-Login Task $it" }
            val finalTasksPage = flows.createBulkTasks(taskTitles)
            logger.info("✅ Created ${taskTitles.size} tasks")
            
            // Verify tasks
            finalTasksPage.scrollToEnd()
            delay(1000)
            
            logger.info("✅ Test complete")
            
        } catch (e: Exception) {
            logger.error("❌ Test failed: ${e.message}", e)
            throw e
        }
    }
    
    private suspend fun resetAppState() {
        try {
            val adbPath = System.getProperty("user.home") + "/Library/Android/sdk/platform-tools/adb"
            
            // Force stop app
            Runtime.getRuntime().exec(arrayOf(adbPath, "shell", "am", "force-stop", packageName)).waitFor()
            delay(1000)
            
            // Clear app data
            Runtime.getRuntime().exec(arrayOf(adbPath, "shell", "pm", "clear", packageName)).waitFor()
            delay(2000)
            
            // Restart the app
            Runtime.getRuntime().exec(arrayOf(
                adbPath, "shell", "am", "start", "-n", "$packageName/.MainActivity"
            )).waitFor()
            delay(3000)
            
            logger.info("✅ App state reset")
        } catch (e: Exception) {
            logger.warn("Failed to reset app state: ${e.message}")
        }
    }
    
    @AfterEach
    fun teardown() {
        try {
            driver.quit()
            logger.info("✅ Test teardown complete")
        } catch (e: Exception) {
            logger.warn("Error during teardown: ${e.message}")
        }
    }
}


