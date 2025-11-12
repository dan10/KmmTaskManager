package com.danioliveira.appium.drivers

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.config.Platform
import org.openqa.selenium.WebDriver

object DriverFactory {
    fun create(config: BenchmarkConfig): WebDriver = when (config.platform) {
        Platform.ANDROID -> AndroidDriverFactory.create(config)
        Platform.IOS -> IOSDriverFactory.create(config)
    }
}


