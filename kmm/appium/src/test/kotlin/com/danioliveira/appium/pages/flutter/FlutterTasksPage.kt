package com.danioliveira.appium.pages.flutter

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * Flutter implementation of TasksPage.
 */
class FlutterTasksPage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractTasksPage(driver, platform, metricsManager) {
    
    override fun verify(): FlutterTasksPage {
        waitForElement(Tags.LIST_TASKS)
        return this
    }
    
    override fun clickAddTask(): AbstractTaskCreatePage {
        clickById(Tags.BTN_ADD_TASK)
        return FlutterTaskCreatePage(driver, platform, metricsManager)
    }
}


