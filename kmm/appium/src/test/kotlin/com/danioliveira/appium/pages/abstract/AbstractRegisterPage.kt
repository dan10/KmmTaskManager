package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.pages.BaseScreen

/**
 * Abstract base class for Register page across all frameworks.
 * Defines the contract for registration actions that all implementations must follow.
 */
abstract class AbstractRegisterPage(
    driver: org.openqa.selenium.WebDriver,
    platform: com.danioliveira.appium.config.Platform,
    metricsManager: com.danioliveira.appium.metrics.MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
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
    fun register(
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


