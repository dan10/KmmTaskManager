package com.danioliveira.appium.pages.flutter

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * Flutter implementation of RegisterPage.
 */
class FlutterRegisterPage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractRegisterPage(driver, platform, metricsManager) {
    
    override fun verify(): FlutterRegisterPage {
        waitForElement(Tags.BTN_REGISTER)
        return this
    }
    
    override fun enterName(name: String): AbstractRegisterPage {
        sendKeysById(Tags.TXT_NAME, name)
        return this
    }
    
    override fun enterEmail(email: String): AbstractRegisterPage {
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    override fun enterPassword(password: String): AbstractRegisterPage {
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    override fun enterConfirmPassword(confirmPassword: String): AbstractRegisterPage {
        sendKeysById(Tags.TXT_CONFIRM_PASSWORD, confirmPassword)
        return this
    }
    
    override fun clickRegister(): AbstractTasksPage {
        clickById(Tags.BTN_REGISTER)
        return FlutterTasksPage(driver, platform, metricsManager)
    }
}


