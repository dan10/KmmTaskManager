package com.danioliveira.appium.pages.abstract

import com.danioliveira.appium.pages.BaseScreen
import com.danioliveira.appium.pages.RegisterPage
import com.danioliveira.appium.pages.TasksPage

/**
 * Abstract base class for Login page across all frameworks.
 * Defines the contract for login actions that all implementations must follow.
 */
abstract class AbstractLoginPage(
    driver: org.openqa.selenium.WebDriver,
    platform: com.danioliveira.appium.config.Platform,
    metricsManager: com.danioliveira.appium.metrics.MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
    /**
     * Enter email address.
     * @param email The email to enter
     * @return This LoginPage instance for fluent chaining
     */
    abstract fun enterEmail(email: String): AbstractLoginPage
    
    /**
     * Enter password.
     * @param password The password to enter
     * @return This LoginPage instance for fluent chaining
     */
    abstract fun enterPassword(password: String): AbstractLoginPage
    
    /**
     * Click the login button.
     * @return TasksPage instance after successful login
     */
    abstract fun clickLogin(): AbstractTasksPage
    
    /**
     * Click the register link to navigate to registration.
     * @return RegisterPage instance
     */
    abstract fun clickRegisterLink(): AbstractRegisterPage
    
    /**
     * Complete login flow with email and password.
     * Fluent method that chains email, password, and login.
     * 
     * @param email The email address
     * @param password The password
     * @return TasksPage instance after successful login
     */
    fun login(email: String, password: String): AbstractTasksPage {
        return enterEmail(email)
            .enterPassword(password)
            .clickLogin()
    }
}


