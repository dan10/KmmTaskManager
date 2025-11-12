package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class TasksPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {
    fun clickAddTask() {
        clickById(Tags.BTN_ADD_TASK)
    }
    
    fun waitForTasksList() {
        waitForElement(Tags.LIST_TASKS)
    }
    
    fun scrollToEnd() {
        val list = findById(Tags.LIST_TASKS)
        // Scroll multiple times to reach end
        repeat(20) {
            driver.findElement(org.openqa.selenium.By.id(Tags.LIST_TASKS))
                .sendKeys(org.openqa.selenium.Keys.PAGE_DOWN)
            Thread.sleep(500)
        }
    }
}

