package com.danioliveira.appium

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.drivers.DriverFactory
import com.danioliveira.appium.scenarios.UserJourneyScenario
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory

class BenchmarkTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var driver: WebDriver
    private lateinit var config: BenchmarkConfig
    
    @BeforeEach
    fun setUp() {
        config = BenchmarkConfig.fromSystemProperties()
        driver = DriverFactory.create(config)
        logger.info("Driver created for ${config.platform} - ${config.app}")
    }
    
    @AfterEach
    fun tearDown() {
        driver.quit()
    }
    
    @Test
    fun userJourney() {
        val scenario = UserJourneyScenario(driver, config)
        
        // Warmup runs
        repeat(config.warmup) { cycle ->
            logger.info("Warmup cycle ${cycle + 1}")
            scenario.executeCycle(cycle + 1)
        }
        
        // Actual benchmark runs
        repeat(config.runs) { cycle ->
            logger.info("Benchmark cycle ${cycle + 1}")
            val startTime = System.currentTimeMillis()
            scenario.executeCycle(cycle + 1)
            val duration = System.currentTimeMillis() - startTime
            logger.info("Cycle ${cycle + 1} completed in ${duration}ms")
        }
    }
}

