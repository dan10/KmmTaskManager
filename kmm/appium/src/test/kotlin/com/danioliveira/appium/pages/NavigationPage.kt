package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class NavigationPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {
    fun navigateToTasks() {
        clickById(Tags.NAV_TASKS)
    }
    
    fun navigateToProjects() {
        clickById(Tags.NAV_PROJECTS)
    }
    
    fun navigateToCalendar() {
        clickById(Tags.NAV_CALENDAR)
    }
}

