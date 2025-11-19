package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Page Object for Registration screen with fluent API.
 * 
 * Example usage:
 * ```
 * val registerScreen = BaseScreen.on<RegisterPage>(driver, platform)
 * val tasksScreen = registerScreen.register("John", "john@example.com", "password123")
 * ```
 */
class RegisterPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BaseScreen(driver, platform, metricsManager) {
    
    override fun verify(): RegisterPage {
        waitForElement(Tags.BTN_REGISTER)
        return this
    }
    
    /**
     * Enter name.
     * @param name The name to enter
     * @return This RegisterPage instance for fluent chaining
     */
    fun enterName(name: String): RegisterPage {
        sendKeysById(Tags.TXT_NAME, name)
        return this
    }
    
    /**
     * Enter email address.
     * @param email The email to enter
     * @return This RegisterPage instance for fluent chaining
     */
    fun enterEmail(email: String): RegisterPage {
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    /**
     * Enter password.
     * @param password The password to enter
     * @return This RegisterPage instance for fluent chaining
     */
    fun enterPassword(password: String): RegisterPage {
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    /**
     * Enter confirm password.
     * @param confirmPassword The password confirmation
     * @return This RegisterPage instance for fluent chaining
     */
    fun enterConfirmPassword(confirmPassword: String): RegisterPage {
        sendKeysById(Tags.TXT_CONFIRM_PASSWORD, confirmPassword)
        return this
    }
    
    /**
     * Click the register button.
     * @return TasksPage instance after successful registration
     */
    fun clickRegister(): TasksPage {
        clickById(Tags.BTN_REGISTER)
        return on<TasksPage>(driver, platform, metricsManager)
    }
    
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
    ): TasksPage {
        return enterName(name)
            .enterEmail(email)
            .enterPassword(password)
            .enterConfirmPassword(confirmPassword)
            .clickRegister()
    }
    
    // Legacy methods for backward compatibility
    @Deprecated("Use verify() instead", ReplaceWith("verify()"))
    fun waitForRegisterScreen() {
        verify()
    }
}

