package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.BaseScreen
import org.openqa.selenium.WebDriver

/**
 * Abstract base class for Register page across all frameworks.
 * Defines the contract for registration actions that all implementations must follow.
 */
abstract class AbstractRegisterPage(
    driver: WebDriver,
    platform: Platform,
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, app, metricsManager) {
    
    /**
     * Enter name.
     * @param name The name to enter
     * @return This RegisterPage instance for fluent chaining
     */
    abstract fun enterName(name: String): AbstractRegisterPage
    
    /**
     * Enter email address.
     * @param email The email to enter
     * @return This RegisterPage instance for fluent chaining
     */
    abstract fun enterEmail(email: String): AbstractRegisterPage
    
    /**
     * Enter password.
     * @param password The password to enter
     * @return This RegisterPage instance for fluent chaining
     */
    abstract fun enterPassword(password: String): AbstractRegisterPage
    
    /**
     * Enter confirm password.
     * @param confirmPassword The password confirmation
     * @return This RegisterPage instance for fluent chaining
     */
    abstract fun enterConfirmPassword(confirmPassword: String): AbstractRegisterPage
    
    /**
     * Click the register button.
     * @return TasksPage instance after successful registration
     */
    abstract fun clickRegister(): AbstractTasksPage
    
    /**
     * Complete registration flow with all required fields.
     * Fluent method that chains all fields and registration.
     * 
     * @param name The user's name
     * @param email The email address
     * @param password The password
     * @param confirmPassword The password confirmation (defaults to password)
     * @return TasksPage instance after successful registration
     */
    open fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String = password
    ): AbstractTasksPage {
        return enterName(name)
            .enterEmail(email)
            .enterPassword(password)
            .enterConfirmPassword(confirmPassword)
            .clickRegister()
    }
}

