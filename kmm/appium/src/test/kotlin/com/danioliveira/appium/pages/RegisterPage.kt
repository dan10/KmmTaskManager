package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
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
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : AbstractRegisterPage(driver, platform, app, metricsManager) {
    
    override fun verify(): RegisterPage {
        waitForElement(Tags.BTN_REGISTER)
        return this
    }
    
    override fun enterName(name: String): RegisterPage {
        clickById(Tags.TXT_NAME)
        sendKeysById(Tags.TXT_NAME, name)
        return this
    }
    
    override fun enterEmail(email: String): RegisterPage {
        clickById(Tags.TXT_EMAIL)
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    override fun enterPassword(password: String): RegisterPage {
        clickById(Tags.TXT_PASSWORD)
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    override fun enterConfirmPassword(confirmPassword: String): RegisterPage {
        clickById(Tags.TXT_CONFIRM_PASSWORD)
        sendKeysById(Tags.TXT_CONFIRM_PASSWORD, confirmPassword)
        return this
    }
    
    override fun clickRegister(): TasksPage {
        clickById(Tags.BTN_REGISTER)
        return on<TasksPage>()
    }
    
    override fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
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

