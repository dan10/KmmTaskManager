package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractLoginPage
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import com.danioliveira.appium.pages.cmp.*
import com.danioliveira.appium.pages.flutter.*
import org.openqa.selenium.WebDriver

/**
 * Factory for creating page objects based on framework type (CMP/Flutter).
 * 
 * This factory ensures the correct page implementation is used for each framework,
 * allowing the same test code to work across different UI frameworks.
 */
object PageFactory {
    
    /**
     * Create a LoginPage instance based on the app type.
     * 
     * @param driver WebDriver instance
     * @param platform Platform (Android/iOS)
     * @param app App type (KMM/CMP or Flutter)
     * @param metricsManager Optional metrics manager
     * @return AbstractLoginPage implementation
     */
    fun createLoginPage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractLoginPage {
        return when (app) {
            App.KMM -> CmpLoginPage(driver, platform, metricsManager)
            App.FLUTTER -> FlutterLoginPage(driver, platform, metricsManager)
        }
    }
    
    /**
     * Create a RegisterPage instance based on the app type.
     */
    fun createRegisterPage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractRegisterPage {
        return when (app) {
            App.KMM -> CmpRegisterPage(driver, platform, metricsManager)
            App.FLUTTER -> FlutterRegisterPage(driver, platform, metricsManager)
        }
    }
    
    /**
     * Create a TasksPage instance based on the app type.
     */
    fun createTasksPage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractTasksPage {
        return when (app) {
            App.KMM -> CmpTasksPage(driver, platform, metricsManager)
            App.FLUTTER -> FlutterTasksPage(driver, platform, metricsManager)
        }
    }
    
    /**
     * Create a TaskCreatePage instance based on the app type.
     */
    fun createTaskCreatePage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractTaskCreatePage {
        return when (app) {
            App.KMM -> CmpTaskCreatePage(driver, platform, metricsManager)
            App.FLUTTER -> FlutterTaskCreatePage(driver, platform, metricsManager)
        }
    }
}


