package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractLoginPage
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
    app: App = App.KMM,
    metricsManager: MetricsManager? = null
) : AbstractLoginPage(driver, platform, app, metricsManager) {
    
    override fun verify(): LoginPage {
        waitForElement(Tags.BTN_LOGIN)
        return this
    }
    
    override fun enterEmail(email: String): LoginPage {
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    override fun enterPassword(password: String): LoginPage {
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    override fun clickLogin(): TasksPage {
        clickById(Tags.BTN_LOGIN)
        return on<TasksPage>()
    }
    
    override fun clickRegisterLink(): RegisterPage {
        try {
            clickById(Tags.LINK_REGISTER)
        } catch (e: Exception) {
            trackAction("click_${Tags.LINK_REGISTER}_fallback") {
                findByText("Sign Up").click()
            }
        }
        return on<RegisterPage>()
    }
    
    override fun login(email: String, password: String): TasksPage {
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

