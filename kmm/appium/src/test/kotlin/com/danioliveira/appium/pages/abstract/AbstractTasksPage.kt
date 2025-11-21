package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.BaseScreen
import org.openqa.selenium.WebDriver

/**
 * Abstract base class for Tasks page across all frameworks.
 * Defines the contract for task-related actions that all implementations must follow.
 */
abstract class AbstractTasksPage(
    driver: WebDriver,
    platform: Platform,
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, app, metricsManager) {
    
    /**
     * Click the add task button to open task creation.
     * @return TaskCreatePage instance
     */
    abstract fun clickAddTask(): AbstractTaskCreatePage
    
    /**
     * Open task creation bottom sheet.
     * Alias for clickAddTask() with more descriptive name.
     * @return TaskCreatePage instance
     */
    open fun openCreateTask(): AbstractTaskCreatePage {
        return clickAddTask()
    }
    
    /**
     * Wait for tasks list to be visible.
     * @return This TasksPage instance for fluent chaining
     */
    open fun waitForTasksList(): AbstractTasksPage {
        verify()
        return this
    }
    
    /**
     * Scroll to the end of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    open fun scrollToEnd(): AbstractTasksPage {
        executeScrollToEnd()
        return this
    }
    
    /**
     * Scroll to the top of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    open fun scrollToTop(): AbstractTasksPage {
        executeScrollToBeginning()
        return this
    }

    /**
     * Scroll to a specific task title.
     * @param taskTitle The title of the task to scroll to
     * @return This TasksPage instance for fluent chaining
     */
    open fun scrollToTask(taskTitle: String): AbstractTasksPage {
        executeScrollToText(taskTitle)
        return this
    }
}

