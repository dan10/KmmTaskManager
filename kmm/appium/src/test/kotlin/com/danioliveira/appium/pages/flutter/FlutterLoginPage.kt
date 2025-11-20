package com.danioliveira.appium.pages.flutter

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractLoginPage
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * Flutter implementation of LoginPage.
 * Uses Flutter-specific locators and element finding strategies.
 * Note: If Flutter uses the same accessibility IDs as CMP, this can share Tags.
 */
class FlutterLoginPage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractLoginPage(driver, platform, metricsManager) {
    
    override fun verify(): FlutterLoginPage {
        waitForElement(Tags.BTN_LOGIN)
        return this
    }
    
    override fun enterEmail(email: String): AbstractLoginPage {
        sendKeysById(Tags.TXT_EMAIL, email)
        return this
    }
    
    override fun enterPassword(password: String): AbstractLoginPage {
        sendKeysById(Tags.TXT_PASSWORD, password)
        return this
    }
    
    override fun clickLogin(): AbstractTasksPage {
        clickById(Tags.BTN_LOGIN)
        return FlutterTasksPage(driver, platform, metricsManager)
    }
    
    override fun clickRegisterLink(): AbstractRegisterPage {
        clickById(Tags.LINK_REGISTER)
        return FlutterRegisterPage(driver, platform, metricsManager)
    }
}


