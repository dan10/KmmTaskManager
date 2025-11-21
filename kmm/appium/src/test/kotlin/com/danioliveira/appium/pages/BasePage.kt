package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import io.appium.java_client.AppiumBy
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.Point
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.PointerInput
import org.openqa.selenium.interactions.Sequence
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration
import java.util.Collections

abstract class BasePage(
    val driver: WebDriver,
    val platform: Platform,
    val app: App = App.KMM,
    val metricsManager: MetricsManager? = null
) {
    protected val wait = WebDriverWait(driver, Duration.ofSeconds(10))

    // Get page name from class name (e.g., "LoginPage" -> "Login")
    protected open val pageName: String
        get() = this::class.simpleName?.removeSuffix("Page") ?: "Unknown"

    private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)

    /**
     * Find element using platform-specific locator strategies with graceful fallbacks.
     *
     * Android:
     *  1. Try UiSelector.resourceId (Compose tags)
     *  2. Fall back to accessibilityId (Flutter semantics / TalkBack tags)
     *
     * iOS:
     *  - accessibilityId
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
            findById(id).run {
                click()
                clear()
                sendKeys(text)
            }
        }
    }

    protected fun waitForElement(id: String, timeoutSeconds: Int = 10) {
        trackAction("waitFor_$id") {
            WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds.toLong()))
                .until {
                    findById(id).isDisplayed
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
            findByClassAndInstance(className, instance).run {
                click()
                clear()
                sendKeys(text)
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
                    try {
                        // Try to find a scrollable element and scroll to end
                        driver.findElement(
                            AppiumBy.androidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true)).scrollToEnd(5)"
                            )
                        )
                    } catch (e: Exception) {
                        // Fallback: Try to scroll using W3C actions or just ignore if already at end
                        log.warn("Failed to scroll to end using UiScrollable: ${e.message}")
                    }
                }

                Platform.IOS -> {
                    val params = HashMap<String, Any>()
                    params["direction"] = "down"
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
     * Scrolls until the specified text is visible.
     */
    protected fun executeScrollToText(text: String) {
        trackAction("scrollToText") {
            when (platform) {
                Platform.ANDROID -> {
                    try {
                        driver.findElement(
                            AppiumBy.androidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains(\"$text\"))"
                            )
                        )
                    } catch (e: Exception) {
                        log.warn("UiScrollable failed to find '$text', falling back to manual swipe: ${e.message}")
                        manualScrollToText(text)
                    }
                }

                Platform.IOS -> {
                    // Simple fallback for iOS - just try to find it, if not scroll down a bit
                    // Real iOS scrolling to element is more complex without predicates
                    try {
                        findByText(text)
                    } catch (e: Exception) {
                        val params = HashMap<String, Any>()
                        params["direction"] = "down"
                        (driver as JavascriptExecutor).executeScript("mobile: scroll", params)
                        findByText(text)
                    }
                }
            }
        }
    }

    private fun manualScrollToText(text: String) {
        var attempts = 0
        val maxAttempts = 5
        while (attempts < maxAttempts) {
            if (isTextVisible(text)) return

            log.info("  👇 Manual swipe down (attempt ${attempts + 1}/$maxAttempts)")
            scrollDown()
            attempts++
        }

        if (!isTextVisible(text)) {
            throw NoSuchElementException("Could not find text '$text' after $maxAttempts manual scrolls")
        }
    }

    private fun isTextVisible(text: String): Boolean {
        return try {
            driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().textContains(\"$text\")")).isDisplayed
        } catch (e: Exception) {
            false
        }
    }

    private fun scrollDown() {
        val dimension = driver.manage().window().size
        val start = Point(dimension.width / 2, (dimension.height * 0.8).toInt())
        val end = Point(dimension.width / 2, (dimension.height * 0.2).toInt())

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val swipe = Sequence(finger, 1)

        swipe.addAction(
            finger.createPointerMove(
                Duration.ofMillis(0),
                PointerInput.Origin.viewport(),
                start.x,
                start.y
            )
        )
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        swipe.addAction(
            finger.createPointerMove(
                Duration.ofMillis(1000),
                PointerInput.Origin.viewport(),
                end.x,
                end.y
            )
        )
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        (driver as org.openqa.selenium.remote.RemoteWebDriver).perform(
            Collections.singletonList(
                swipe
            )
        )
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

    private fun locatorsById(id: String): List<By> {
        return when (app) {
            App.FLUTTER -> {
                // For Flutter apps, use flutterKey first, then fallback to platform-specific locators
                when (platform) {
                    Platform.ANDROID -> listOf(
                        AppiumBy.flutterKey(id),
                        AppiumBy.accessibilityId(id),
                        AppiumBy.androidUIAutomator("new UiSelector().description(\"$id\")"),
                        AppiumBy.xpath("//*[@name='$id']")
                    )

                    Platform.IOS -> listOf(
                        AppiumBy.flutterKey(id),
                        AppiumBy.accessibilityId(id)
                    )
                }
            }

            App.KMM -> {
                // For KMM/Compose apps, use platform-specific locators
                when (platform) {
                    Platform.ANDROID -> listOf(
                        AppiumBy.androidUIAutomator("new UiSelector().resourceIdMatches(\".*$id\")"),
                        AppiumBy.accessibilityId(id),
                        AppiumBy.xpath("//*[@name='$id']")
                    )

                    Platform.IOS -> listOf(AppiumBy.accessibilityId(id))
                }
            }
        }
    }
}
