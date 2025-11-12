package com.danioliveira.appium

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.drivers.DriverFactory
import com.danioliveira.appium.pages.LoginPage
import io.appium.java_client.android.AndroidDriver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory

/**
 * Incremental test for LoginPage only.
 * This allows you to test and verify the login flow in isolation.
 * 
 * Usage:
 * ./gradlew :appium:test --tests IncrementalLoginTest \
 *   -Dplatform=android \
 *   -Dapp=kmm \
 *   -DapkPath=/path/to/app-profile.apk \
 *   -DappPackage=com.danioliveira.taskmanager \
 *   -DdeviceName="emulator-5554"
 */
class IncrementalLoginTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var config: BenchmarkConfig
    private lateinit var loginPage: LoginPage

    @BeforeEach
    fun setUp() {
        config = BenchmarkConfig.fromSystemProperties()
        driver = DriverFactory.create(config)
        driver.manage()
        AndroidDriver.builder()
        loginPage = LoginPage(driver, config.platform)
        logger.info("Driver created for ${config.platform} - ${config.app}")
    }

    @AfterEach
    fun tearDown() {
        driver.quit()
    }

    @Test
    fun testLoginPageElementsVisible() {
        logger.info("Testing LoginPage elements visibility")
        
        // Wait for login screen to be visible
        loginPage.waitForLoginScreen()
        logger.info("✓ Login screen is visible")
        
        // Try to interact with elements to verify they're present
        try {
            loginPage.enterEmail("")
            logger.info("✓ Email field is accessible")
        } catch (e: Exception) {
            throw AssertionError("Email field should be accessible", e)
        }
        
        try {
            loginPage.enterPassword("")
            logger.info("✓ Password field is accessible")
        } catch (e: Exception) {
            throw AssertionError("Password field should be accessible", e)
        }
        
        logger.info("✅ All LoginPage elements are visible and accessible")
    }

    @Test
    fun testLoginPageInteraction() {
        logger.info("Testing LoginPage interaction")
        
        // Wait for login screen
        loginPage.waitForLoginScreen()
        
        // Type in email field
        logger.info("Typing email...")
        loginPage.enterEmail("test@example.com")
        logger.info("✓ Email typed successfully")
        
        // Type in password field
        logger.info("Typing password...")
        loginPage.enterPassword("password123")
        logger.info("✓ Password typed successfully")
        
        // Click login button (this will likely fail auth, but we're testing interaction)
        logger.info("Clicking login button...")
        loginPage.clickLogin()
        logger.info("✓ Login button clicked successfully")
        
        // Wait a bit to see what happens
        Thread.sleep(2000)
        
        logger.info("✅ LoginPage interaction test completed")
    }

    @Test
    fun testLoginPageNavigation() {
        logger.info("Testing LoginPage navigation")
        
        // Wait for login screen
        loginPage.waitForLoginScreen()
        
        // Click register link
        logger.info("Clicking register link...")
        loginPage.clickRegisterLink()
        logger.info("✓ Register link clicked successfully")
        
        // Wait a bit to see navigation
        Thread.sleep(2000)
        
        // Navigate back (if needed)
        logger.info("Navigating back...")
        driver.navigate().back()
        
        // Verify we're back on login screen
        loginPage.waitForLoginScreen()
        logger.info("✓ Successfully navigated back to login screen")
        
        logger.info("✅ LoginPage navigation test completed")
    }

    @Test
    fun testLoginPagePerformance() {
        logger.info("Testing LoginPage performance")
        
        val iterations = 5
        val timings = mutableListOf<Long>()
        
        repeat(iterations) { i ->
            logger.info("Iteration ${i + 1}/$iterations")
            
            val startTime = System.currentTimeMillis()
            
            // Wait for login screen
            loginPage.waitForLoginScreen()
            
            // Type credentials
            loginPage.enterEmail("test$i@example.com")
            loginPage.enterPassword("password123")
            
            // Click login
            loginPage.clickLogin()
            
            val duration = System.currentTimeMillis() - startTime
            timings.add(duration)
            
            logger.info("Iteration ${i + 1} took ${duration}ms")
            
            // Wait a bit before next iteration
            Thread.sleep(1000)
            
            // If we navigated away, go back
            try {
                driver.navigate().back()
            } catch (e: Exception) {
                // Might already be on login screen
            }
        }
        
        val avgTime = timings.average()
        val minTime = timings.minOrNull() ?: 0
        val maxTime = timings.maxOrNull() ?: 0
        
        logger.info("✅ LoginPage performance test completed")
        logger.info("Average time: ${avgTime}ms")
        logger.info("Min time: ${minTime}ms")
        logger.info("Max time: ${maxTime}ms")
    }
}

