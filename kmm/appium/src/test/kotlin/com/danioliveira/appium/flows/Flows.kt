package com.danioliveira.appium.flows

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.*
import org.openqa.selenium.WebDriver
import kotlinx.coroutines.delay

/**
 * Fluent DSL for composing business-readable test flows.
 * 
 * Provides high-level methods that compose page object actions
 * into meaningful business scenarios.
 * 
 * Example usage:
 * ```
 * val flows = Flows(driver, platform)
 * flows.registerAndCreateTask("John", "john@example.com", "password", "My Task")
 * ```
 */
class Flows(
    private val driver: WebDriver,
    private val platform: Platform,
    private val metricsManager: MetricsManager? = null
) {
    
    /**
     * Register a new user and navigate to tasks screen.
     * 
     * @param name User's name
     * @param email User's email (must be unique)
     * @param password User's password
     * @return TasksPage instance after successful registration
     */
    fun registerUser(
        name: String,
        email: String,
        password: String
    ): TasksPage {
        return BaseScreen.on<RegisterPage>(driver, platform, metricsManager)
            .register(name, email, password)
    }
    
    /**
     * Login with existing credentials.
     * 
     * @param email User's email
     * @param password User's password
     * @return TasksPage instance after successful login
     */
    fun login(email: String, password: String): TasksPage {
        return BaseScreen.on<LoginPage>(driver, platform, metricsManager)
            .login(email, password)
    }
    
    /**
     * Create a task from the tasks screen.
     * Opens the create form, enters title/description, and saves.
     * 
     * @param title Task title (required)
     * @param description Task description (optional)
     * @return TasksPage instance after task is created
     */
    suspend fun createTask(title: String, description: String? = null): TasksPage {
        return BaseScreen.on<TasksPage>(driver, platform, metricsManager)
            .openCreateTask()
           .createTask(title, description)
    }
    
    /**
     * Create multiple tasks quickly (for bulk operations).
     * Uses instance-based finding for reliability.
     * 
     * @param titles List of task titles to create
     * @return TasksPage instance after all tasks are created
     */
    suspend fun createBulkTasks(titles: List<String>): TasksPage {
        var tasksPage = BaseScreen.on<TasksPage>(driver, platform, metricsManager)
        
        for (title in titles) {
            val createPage = tasksPage.openCreateTask()
            delay(800) // Give bottom sheet time to appear
            tasksPage = createPage.quickCreateByInstance(title)
            delay(500) // Brief delay between creations
        }
        
        return tasksPage
    }
    
    /**
     * Complete registration and create a task in one flow.
     * 
     * @param name User's name
     * @param email User's email (must be unique)
     * @param password User's password
     * @param taskTitle Task title to create
     * @param taskDescription Optional task description
     * @return TasksPage instance after task is created
     */
    suspend fun registerAndCreateTask(
        name: String,
        email: String,
        password: String,
        taskTitle: String,
        taskDescription: String? = null
    ): TasksPage {
        val tasksPage = registerUser(name, email, password)
        delay(500) // Brief delay after registration
        
        val createPage = tasksPage.openCreateTask()
        delay(800) // Give bottom sheet time to appear
        return createPage.createTask(taskTitle, taskDescription)
    }
    
    /**
     * Create multiple tasks after login.
     * 
     * @param email User's email
     * @param password User's password
     * @param taskTitles List of task titles to create
     * @return TasksPage instance after all tasks are created
     */
    suspend fun loginAndCreateTasks(
        email: String,
        password: String,
        taskTitles: List<String>
    ): TasksPage {
        val tasksPage = login(email, password)
        delay(500) // Brief delay after login
        
        return createBulkTasks(taskTitles)
    }
    
    /**
     * Scroll to end of tasks list to verify all tasks are visible.
     * 
     * @return TasksPage instance
     */
    fun scrollToEndOfTasks(): TasksPage {
        return BaseScreen.on<TasksPage>(driver, platform, metricsManager)
            .scrollToEnd()
    }
}

