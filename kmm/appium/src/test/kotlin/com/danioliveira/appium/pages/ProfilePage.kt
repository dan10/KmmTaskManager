package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class ProfilePage(
    driver: WebDriver, 
    platform: Platform,
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, app, metricsManager) {
    fun clickLogout() {
        clickById(Tags.BTN_LOGOUT)
    }
    
    fun waitForProfileScreen() {
        waitForElement(Tags.BTN_LOGOUT)
    }
}

