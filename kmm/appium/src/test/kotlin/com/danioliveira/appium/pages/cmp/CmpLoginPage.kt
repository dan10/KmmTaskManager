package com.danioliveira.appium.pages.cmp

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractLoginPage
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * CMP (Compose Multiplatform) implementation of LoginPage.
 * Uses CMP-specific locators and element finding strategies.
 */
class CmpLoginPage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractLoginPage(driver, platform, metricsManager) {
    
    override fun verify(): CmpLoginPage {
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
        return CmpTasksPage(driver, platform, metricsManager)
    }
    
    override fun clickRegisterLink(): AbstractRegisterPage {
        clickById(Tags.LINK_REGISTER)
        return CmpRegisterPage(driver, platform, metricsManager)
    }
}


