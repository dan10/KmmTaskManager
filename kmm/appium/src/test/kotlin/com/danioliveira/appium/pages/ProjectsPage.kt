package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class ProjectsPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {
    fun clickAddProject() {
        clickById(Tags.BTN_ADD_PROJECT)
    }
    
    fun waitForProjectsList() {
        waitForElement(Tags.LIST_PROJECTS)
    }
    
    fun clickProject(projectId: String) {
        clickById(Tags.projectCard(projectId))
    }
}

