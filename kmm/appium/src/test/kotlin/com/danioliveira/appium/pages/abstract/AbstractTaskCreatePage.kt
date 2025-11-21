package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.BaseScreen
import org.openqa.selenium.WebDriver

/**
 * Abstract base class for Task Creation page across all frameworks.
 * Defines the contract for task creation actions that all implementations must follow.
 */
abstract class AbstractTaskCreatePage(
    driver: WebDriver,
    platform: Platform,
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, app, metricsManager) {
    
    /**
     * Wait for the task creation form to be visible.
     * @return This TaskCreatePage instance for fluent chaining
     */
    abstract fun waitForCreateForm(): AbstractTaskCreatePage
    
    /**
     * Enter task title.
     * @param title The task title to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    abstract fun enterTitle(title: String): AbstractTaskCreatePage
    
    /**
     * Enter task description.
     * @param description The task description to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    abstract fun enterDescription(description: String): AbstractTaskCreatePage
    
    /**
     * Click the save button to create the task.
     * @return TasksPage instance after task is created
     */
    abstract fun clickSave(): AbstractTasksPage
    
    /**
     * Click the cancel button to dismiss the form.
     * @return TasksPage instance after canceling
     */
    abstract fun clickCancel(): AbstractTasksPage
    
    /**
     * Complete flow: Enter title, description (optional), and save.
     * Fluent method that chains all steps.
     * 
     * @param title Required task title
     * @param description Optional task description
     * @return TasksPage instance after task is created
     */
    open fun createTask(title: String, description: String? = null): AbstractTasksPage {
        return waitForCreateForm()
            .enterTitle(title)
            .let { page ->
                if (description != null) {
                    page.enterDescription(description)
                } else {
                    page
                }
            }
            .clickSave()
    }
    
    /**
     * Quick create: Just title and save (for bulk creation).
     * Fluent method optimized for speed.
     * 
     * @param title The task title
     * @return TasksPage instance after task is created
     */
    open fun quickCreate(title: String): AbstractTasksPage {
        return enterTitle(title)
            .clickSave()
    }
    
    /**
     * Quick create using instance-based finding (fallback when test tags don't work).
     * Just enters title and saves (for bulk creation).
     * 
     * @param title The task title
     * @return TasksPage instance after task is created
     */
    abstract fun quickCreateByInstance(title: String): AbstractTasksPage
}

