package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Base class for all screen/page objects using fluent design pattern.
 * 
 * Provides fluent navigation between screens using the `on<T>()` method
 * which instantiates and verifies screens automatically.
 * 
 * Example usage:
 * ```
 * val loginScreen = BaseScreen.on<LoginScreen>(driver, platform)
 * val tasksScreen = loginScreen.login("user@example.com", "password")
 * ```
 */
open class BaseScreen(
      driver: WebDriver,
      platform: Platform,
      app: App = App.KMM,
      metricsManager: MetricsManager? = null
) : BasePage(driver, platform, app, metricsManager) {

    companion object {
        inline fun <reified T : BaseScreen> on(
            driver: WebDriver,
            platform: Platform,
            metricsManager: MetricsManager? = null,
            verifyScreen: Boolean = true
        ): T = on(driver, platform, App.KMM, metricsManager, verifyScreen)

        inline fun <reified T : BaseScreen> on(
            driver: WebDriver,
            platform: Platform,
            app: App,
            metricsManager: MetricsManager? = null,
            verifyScreen: Boolean = true
        ): T {
            val screen = T::class.java
                .getDeclaredConstructor(
                    WebDriver::class.java,
                    Platform::class.java,
                    App::class.java,
                    MetricsManager::class.java
                )
                .newInstance(driver, platform, app, metricsManager)

            if (verifyScreen) {
                screen.verify()
            }

            return screen
        }
    }
    
    /**
     * Navigate to a new screen/page object.
     * Instantiates the target screen and optionally verifies it's loaded.
     * 
     * @param verifyScreen If true, calls verify() on the new screen to ensure it's loaded
     * @return The new screen instance
     */
    inline fun <reified T : BaseScreen> on(verifyScreen: Boolean = true): T {
        val screen = T::class.java
            .getDeclaredConstructor(
                WebDriver::class.java,
                Platform::class.java,
                App::class.java,
                MetricsManager::class.java
            )
            .newInstance(driver, platform, app, metricsManager)
        
        if (verifyScreen) {
            screen.verify()
        }
        
        return screen
    }
    
    /**
     * Verify that the current screen is loaded and ready.
     * Override this method in subclasses to check for key elements.
     * 
     * @return This screen instance for fluent chaining
     */
    open fun verify(): BaseScreen {
        return this
    }
    
    /**
     * Wait for the screen to be loaded.
     * Convenience method that calls verify().
     * 
     * @return This screen instance for fluent chaining
     */
    fun waitForLoaded(): BaseScreen {
        return verify()
    }
}

