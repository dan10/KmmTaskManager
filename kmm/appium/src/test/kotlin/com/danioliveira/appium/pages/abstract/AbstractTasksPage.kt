package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.pages.BaseScreen

/**
 * Abstract base class for Tasks page across all frameworks.
 * Defines the contract for task-related actions that all implementations must follow.
 */
abstract class AbstractTasksPage(
    driver: org.openqa.selenium.WebDriver,
    platform: com.danioliveira.appium.config.Platform,
    metricsManager: com.danioliveira.appium.metrics.MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
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
    fun openCreateTask(): AbstractTaskCreatePage {
        return clickAddTask()
    }
    
    /**
     * Wait for tasks list to be visible.
     * @return This TasksPage instance for fluent chaining
     */
    fun waitForTasksList(): AbstractTasksPage {
        verify()
        return this
    }
    
    /**
     * Scroll to the end of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    fun scrollToEnd(): AbstractTasksPage {
        executeScrollToEnd()
        return this
    }
    
    /**
     * Scroll to the top of the tasks list.
     * @return This TasksPage instance for fluent chaining
     */
    fun scrollToTop(): AbstractTasksPage {
        executeScrollToBeginning()
        return this
    }

    /**
     * Scroll to a specific task title.
     * @param taskTitle The title of the task to scroll to
     * @return This TasksPage instance for fluent chaining
     */
    fun scrollToTask(taskTitle: String): AbstractTasksPage {
        executeScrollToText(taskTitle)
        return this
    }
}


