package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Page Object for Task Creation form with fluent API.
 * Handles interactions with the task creation bottom sheet/modal.
 * 
 * Example usage:
 * ```
 * val createScreen = tasksScreen.openCreateTask()
 * val tasksScreen = createScreen.createTask("My Task", "Description")
 * ```
 */
class TaskCreatePage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {

    override fun verify(): TaskCreatePage {
        // Try ID-based first, fall back to instance-based
        try {
            waitForElement(Tags.TXT_TASK_TITLE)
        } catch (e: Exception) {
            waitForCreateFormByInstance()
        }
        return this
    }

    /**
     * Wait for the task creation form to be visible.
     * Waits for the title field to appear as an indicator.
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun waitForCreateForm(): TaskCreatePage {
        try {
            waitForElement(Tags.TXT_TASK_TITLE)
        } catch (e: Exception) {
            waitForCreateFormByInstance()
        }
        return this
    }

    /**
     * Enter task title.
     * @param title The task title to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun enterTitle(title: String): TaskCreatePage {
        try {
            sendKeysById(Tags.TXT_TASK_TITLE, title)
        } catch (e: Exception) {
            enterTitleByInstance(title)
        }
        return this
    }

    /**
     * Enter task description.
     * @param description The task description to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun enterDescription(description: String): TaskCreatePage {
        try {
            sendKeysById(Tags.TXT_TASK_DESCRIPTION, description)
        } catch (e: Exception) {
            enterDescriptionByInstance(description)
        }
        return this
    }

    /**
     * Click the save button to create the task.
     * @return TasksPage instance after task is created
     */
    fun clickSave(): TasksPage {
        try {
            clickById(Tags.BTN_SAVE)
        } catch (e: Exception) {
            clickSaveByInstance()
        }
        return on<TasksPage>()
    }

    /**
     * Click the cancel button to dismiss the form.
     * @return TasksPage instance after canceling
     */
    fun clickCancel(): TasksPage {
        clickById(Tags.BTN_CANCEL)
        return on<TasksPage>()
    }

    /**
     * Complete flow: Enter title, description (optional), and save.
     * Fluent method that chains all steps.
     * 
     * @param title Required task title
     * @param description Optional task description
     * @return TasksPage instance after task is created
     */
    fun createTask(title: String, description: String? = null): TasksPage {
        return trackAction("createTask") {
            waitForCreateForm()
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
    }

    /**
     * Quick create: Just title and save (for bulk creation).
     * Fluent method optimized for speed.
     * 
     * @param title The task title
     * @return TasksPage instance after task is created
     */
    fun quickCreate(title: String): TasksPage {
        return trackAction("quickCreateTask") {
            enterTitle(title)
                .clickSave()
        }
    }
    
    // ============================================================================
    // Instance-based methods (fallback when test tags don't work)
    // ============================================================================
    

    
    /**
     * Wait for the task creation form using instance-based finding.
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun waitForCreateFormByInstance(): TaskCreatePage {
        trackAction("waitForCreateForm_instance") {
            try {
                findEditTextByInstance(0)
            } catch (e: Exception) {
                throw Exception("Task create form not visible - first EditText not found", e)
            }
        }
        return this
    }
    
    /**
     * Enter task title using instance-based finding.
     * Assumes title field is the first EditText (instance 0).
     * 
     * @param title The task title to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun enterTitleByInstance(title: String): TaskCreatePage {
        sendKeysToEditTextByInstance(0, title)
        return this
    }
    
    /**
     * Enter task description using instance-based finding.
     * Assumes description field is the second EditText (instance 1).
     * 
     * @param description The task description to enter
     * @return This TaskCreatePage instance for fluent chaining
     */
    fun enterDescriptionByInstance(description: String): TaskCreatePage {
        sendKeysToEditTextByInstance(1, description)
        return this
    }
    
    /**
     * Click the save button using instance-based finding.
     * Assumes save button is the 4th Button (instance 3).
     * @return TasksPage instance after task is created
     */
    fun clickSaveByInstance(): TasksPage {
        clickButtonByInstance(3)
        return on<TasksPage>()
    }
    
    /**
     * Quick create using instance-based finding.
     * Just enters title and saves (for bulk creation).
     * 
     * @param title The task title
     * @return TasksPage instance after task is created
     */
    fun quickCreateByInstance(title: String): TasksPage {
        return trackAction("quickCreateTask_instance") {
            enterTitleByInstance(title)
                .clickSaveByInstance()
        }
    }
    
    /**
     * Complete flow using instance-based finding.
     * Enters title, description (optional), and saves.
     * 
     * @param title Required task title
     * @param description Optional task description
     * @return TasksPage instance after task is created
     */
    fun createTaskByInstance(title: String, description: String? = null): TasksPage {
        return trackAction("createTask_instance") {
            waitForCreateFormByInstance()
                .enterTitleByInstance(title)
                .let { page ->
                    if (description != null) {
                        page.enterDescriptionByInstance(description)
                    } else {
                        page
                    }
                }
                .clickSaveByInstance()
        }
    }
}