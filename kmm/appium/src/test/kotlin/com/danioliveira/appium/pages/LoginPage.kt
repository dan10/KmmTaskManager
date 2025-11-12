package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

class LoginPage(
    driver: WebDriver, 
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {
    
    fun enterEmail(email: String) {
        sendKeysById(Tags.TXT_EMAIL, email)
    }
    
    fun enterPassword(password: String) {
        sendKeysById(Tags.TXT_PASSWORD, password)
    }
    
    fun clickLogin() {
        clickById(Tags.BTN_LOGIN)
    }
    
    fun clickRegisterLink() {
        clickById(Tags.LINK_REGISTER)
    }
    
    fun waitForLoginScreen() {
        waitForElement(Tags.BTN_LOGIN)
    }
}

