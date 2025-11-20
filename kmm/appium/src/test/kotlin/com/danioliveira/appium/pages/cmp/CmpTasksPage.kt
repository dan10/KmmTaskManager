package com.danioliveira.appium.pages.cmp

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * CMP (Compose Multiplatform) implementation of TasksPage.
 */
class CmpTasksPage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractTasksPage(driver, platform, metricsManager) {
    
    override fun verify(): CmpTasksPage {
        waitForElement(Tags.LIST_TASKS)
        return this
    }
    
    override fun clickAddTask(): AbstractTaskCreatePage {
        clickById(Tags.BTN_ADD_TASK)
        return CmpTaskCreatePage(driver, platform, metricsManager)
    }
}


