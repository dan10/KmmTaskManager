package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class RegisterPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {
    
    fun enterName(name: String) {
        sendKeysById(Tags.TXT_NAME, name)
    }
    
    fun enterEmail(email: String) {
        sendKeysById(Tags.TXT_EMAIL, email)
    }
    
    fun enterPassword(password: String) {
        sendKeysById(Tags.TXT_PASSWORD, password)
    }
    
    fun enterConfirmPassword(confirmPassword: String) {
        sendKeysById(Tags.TXT_CONFIRM_PASSWORD, confirmPassword)
    }
    
    fun clickRegister() {
        clickById(Tags.BTN_REGISTER)
    }
    
    fun waitForRegisterScreen() {
        waitForElement(Tags.BTN_REGISTER)
    }
}

