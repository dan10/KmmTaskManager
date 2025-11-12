package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import io.appium.java_client.AppiumBy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

abstract class BasePage(
    protected val driver: WebDriver,
    protected val platform: Platform,
    protected val metricsManager: MetricsManager? = null
) {
    protected val wait = WebDriverWait(driver, Duration.ofSeconds(10))
    
    // Get page name from class name (e.g., "LoginPage" -> "Login")
    protected open val pageName: String
        get() = this::class.simpleName?.removeSuffix("Page") ?: "Unknown"
    
    /**
     * Find element using platform-specific locator strategies.
     * - Android: Uses AppiumBy.androidUIAutomator with UiSelector.resourceId()
     * - iOS: Uses AppiumBy.accessibilityId()
     */
    protected fun findById(id: String): WebElement {
        return wait.until {
            when (platform) {
                Platform.ANDROID -> {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"$id\")"))
                }
                Platform.IOS -> {
                    driver.findElement(AppiumBy.accessibilityId(id))
                }
            }
        }
    }
    
    /**
     * Find element by text content using platform-specific locators.
     */
    protected fun findByText(text: String): WebElement {
        return wait.until {
            when (platform) {
                Platform.ANDROID -> {
                    driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"$text\")"))
                }
                Platform.IOS -> {
                    driver.findElement(AppiumBy.iOSNsPredicateString("label == \"$text\""))
                }
            }
        }
    }
    
    protected fun clickById(id: String) {
        trackAction("click_$id") {
            findById(id).click()
        }
    }
    
    protected fun sendKeysById(id: String, text: String) {
        trackAction("sendKeys_$id") {
            val element = findById(id)
            element.clear()
            element.sendKeys(text)
        }
    }
    
    protected fun waitForElement(id: String, timeoutSeconds: Int = 10) {
        trackAction("waitFor_$id") {
            WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds.toLong()))
                .until { 
                    try {
                        findById(id).isDisplayed
                    } catch (e: Exception) {
                        false
                    }
                }
        }
    }
    
    /**
     * Track any custom action with metrics collection
     */
    protected fun <T> trackAction(actionName: String, block: () -> T): T {
        return if (metricsManager != null) {
            var result: T? = null
            metricsManager.trackAction(pageName, actionName) {
                result = block()
            }
            result!!
        } else {
            block()
        }
    }
}

