package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
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
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : AbstractTasksPage(driver, platform, app, metricsManager) {
    
    override fun verify(): TasksPage {
        waitForElement(Tags.LIST_TASKS)
        return this
    }
    
    override fun clickAddTask(): TaskCreatePage {
        clickById(Tags.BTN_ADD_TASK)
        return on<TaskCreatePage>(verifyScreen = false)
    }
    
    override fun openCreateTask(): TaskCreatePage {
        return clickAddTask()
    }
    
    override fun waitForTasksList(): TasksPage {
        verify()
        return this
    }
    
    override fun scrollToEnd(): TasksPage {
        executeScrollToEnd()
        return this
    }
    
    override fun scrollToTop(): TasksPage {
        executeScrollToBeginning()
        return this
    }
    
    override fun scrollToTask(taskTitle: String): TasksPage {
        executeScrollToText(taskTitle)
        return this
    }
}
