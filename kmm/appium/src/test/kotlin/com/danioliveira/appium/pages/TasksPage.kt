package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Page Object for Tasks screen with fluent API.
 * 
 * Example usage:
 * ```
 * val tasksScreen = BaseScreen.on<TasksPage>(driver, platform)
 * val createScreen = tasksScreen.openCreateTask()
 * ```
 */
class TasksPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
    override fun verify(): TasksPage {
        waitForElement(Tags.LIST_TASKS)
        return this
    }
    
    /**
     * Click the add task button to open task creation.
     * @return TaskCreatePage instance
     */
    fun clickAddTask(): TaskCreatePage {
        clickById(Tags.BTN_ADD_TASK)
        return on<TaskCreatePage>(verifyScreen = false) // Bottom sheet may not be immediately ready
    }
    
    /**
     * Open task creation bottom sheet.
     * Alias for clickAddTask() with more descriptive name.
     * @return TaskCreatePage instance
     */
    fun openCreateTask(): TaskCreatePage {
        return clickAddTask()
    }
    
    /**
     * Wait for tasks list to be visible.
     * @return This TasksPage instance for fluent chaining
     */
    fun waitForTasksList(): TasksPage {
        verify()
        return this
    }
    
    /**
     * Scroll to the end of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    fun scrollToEnd(): TasksPage {
        executeScrollToEnd()
        return this
    }
    
    /**
     * Scroll to the top of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    fun scrollToTop(): TasksPage {
        executeScrollToBeginning()
        return this
    }
}
