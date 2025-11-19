package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Page Object for Login screen with fluent API.
 * 
 * Example usage:
 * ```
 * val loginScreen = BaseScreen.on<LoginPage>(driver, platform)
 * val tasksScreen = loginScreen.login("user@example.com", "password")
 * ```
 */
class LoginPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
    override fun verify(): LoginPage {
        waitForElement(Tags.BTN_LOGIN)
        return this
    }
    
    /**
     * Enter email address.
     * @param email The email to enter
     * @return This LoginPage instance for fluent chaining
     */
    fun enterEmail(email: String): LoginPage {
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    /**
     * Enter password.
     * @param password The password to enter
     * @return This LoginPage instance for fluent chaining
     */
    fun enterPassword(password: String): LoginPage {
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    /**
     * Click the login button.
     * @return TasksPage instance after successful login
     */
    fun clickLogin(): TasksPage {
        clickById(Tags.BTN_LOGIN)
        return on<TasksPage>(driver, platform, metricsManager)
    }
    
    /**
     * Click the register link to navigate to registration.
     * @return RegisterPage instance
     */
    fun clickRegisterLink(): RegisterPage {
        clickById(Tags.LINK_REGISTER)
        return on<RegisterPage>(driver, platform, metricsManager)
    }
    
    /**
     * Complete login flow with email and password.
     * Fluent method that chains email, password, and login.
     * 
     * @param email The email address
     * @param password The password
     * @return TasksPage instance after successful login
     */
    fun login(email: String, password: String): TasksPage {
        return enterEmail(email)
            .enterPassword(password)
            .clickLogin()
    }
    
    // Legacy methods for backward compatibility
    @Deprecated("Use verify() instead", ReplaceWith("verify()"))
    fun waitForLoginScreen() {
        verify()
    }
}

