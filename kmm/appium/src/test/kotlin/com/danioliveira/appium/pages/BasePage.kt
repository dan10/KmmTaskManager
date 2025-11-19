package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import io.appium.java_client.AppiumBy
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.TimeoutException
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

    private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    
    /**
     * Find element using platform-specific locator strategies.
     * - Android: Uses AppiumBy.androidUIAutomator with UiSelector.resourceId()
     * - iOS: Uses AppiumBy.accessibilityId()
     */
    protected fun findById(id: String): WebElement {
        return try {
            wait.until {
                when (platform) {
                    Platform.ANDROID -> {
                        driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"$id\")"))
                    }

                    Platform.IOS -> {
                        driver.findElement(AppiumBy.accessibilityId(id))
                    }
                }
            }
        } catch (e: TimeoutException) {
            log.error("Element with id $id not found within timeout")
            throw e
        } catch (e: NoSuchElementException) {
            log.error("Element with id $id not found")
            throw e
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
     * Find element by className and instance.
     * Useful when test tags are not exposed properly in the UI hierarchy.
     * 
     * @param className The Android class name (e.g., "android.widget.EditText")
     * @param instance The 0-based index of the element (0 = first, 1 = second, etc.)
     * @return The found WebElement
     */
    protected fun findByClassAndInstance(className: String, instance: Int): WebElement {
        log.info("🔍 Finding $className at instance $instance")
        return wait.until {
            try {
                 driver.findElement(
                    AppiumBy.androidUIAutomator(
                        "new UiSelector().className(\"$className\").instance($instance)"
                    )
                )
            } catch (e: Exception) {
                log.debug("  ❌ Failed to find $className at instance $instance: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Find EditText by instance index (position in hierarchy).
     * Convenience method for finding input fields when test tags don't work.
     * 
     * @param instance The 0-based index of the EditText (0 = first, 1 = second, etc.)
     * @return The found WebElement
     */
    protected fun findEditTextByInstance(instance: Int): WebElement {
        return findByClassAndInstance("android.widget.EditText", instance)
    }
    
    /**
     * Find Button by instance index (position in hierarchy).
     * Convenience method for finding buttons when test tags don't work.
     * 
     * @param instance The 0-based index of the Button (0 = first, 1 = second, etc.)
     * @return The found WebElement
     */
    protected fun findButtonByInstance(instance: Int): WebElement {
        return findByClassAndInstance("android.widget.Button", instance)
    }
    
    /**
     * Send keys to an element found by className and instance.
     * 
     * @param className The Android class name
     * @param instance The 0-based index of the element
     * @param text The text to send
     */
    protected fun sendKeysByClassAndInstance(className: String, instance: Int, text: String) {
        trackAction("sendKeys_${className}_$instance") {
            try {
                log.info("⌨️  Sending keys to $className[$instance]: '${text.take(50)}${if (text.length > 50) "..." else ""}'")
                val element = findByClassAndInstance(className, instance)
                log.debug("  Element found, clearing existing text...")
                element.clear()
                log.debug("  Sending keys...")
                element.sendKeys(text)
                log.info("  ✅ Successfully sent keys to $className[$instance]")
            } catch (e: Exception) {
                log.error("❌ Failed to send keys to $className[$instance]: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Send keys to EditText by instance index.
     * Convenience method for sending text to input fields when test tags don't work.
     * 
     * @param instance The 0-based index of the EditText (0 = first, 1 = second, etc.)
     * @param text The text to send
     */
    protected fun sendKeysToEditTextByInstance(instance: Int, text: String) {
        sendKeysByClassAndInstance("android.widget.EditText", instance, text)
    }
    
    /**
     * Click button by instance index.
     * Convenience method for clicking buttons when test tags don't work.
     * 
     * @param instance The 0-based index of the Button (0 = first, 1 = second, etc.)
     */
    protected fun clickButtonByInstance(instance: Int) {
        trackAction("click_Button_$instance") {
            try {
                log.info("👆 Clicking Button at instance $instance")
                val element = findButtonByInstance(instance)
                element.click()
                log.info("  ✅ Successfully clicked Button[$instance]")
            } catch (e: Exception) {
                log.error("❌ Failed to click Button[$instance]: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Scrolls to the end of a scrollable view.
     */
    protected fun executeScrollToEnd() {
        trackAction("scrollToEnd") {
            when (platform) {
                Platform.ANDROID -> {
                    driver.findElement(
                        AppiumBy.androidUIAutomator(
                            "new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(10)"
                        )
                    )
                }
                Platform.IOS -> {
                    val params = mapOf("direction" to "down")
                    (driver as JavascriptExecutor).executeScript("mobile: scroll", params)
                }
            }
        }
    }

    /**
     * Scrolls to the beginning of a scrollable view.
     */
    protected fun executeScrollToBeginning() {
        trackAction("scrollToBeginning") {
            when (platform) {
                Platform.ANDROID -> {
                    driver.findElement(
                        AppiumBy.androidUIAutomator(
                            "new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(10)"
                        )
                    )
                }
                Platform.IOS -> {
                    val params = mapOf("direction" to "up")
                    (driver as JavascriptExecutor).executeScript("mobile: scroll", params)
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
