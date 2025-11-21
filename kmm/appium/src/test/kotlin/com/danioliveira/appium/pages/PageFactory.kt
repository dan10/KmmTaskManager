package com.danioliveira.appium.pages

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractLoginPage
import com.danioliveira.appium.pages.abstract.AbstractRegisterPage
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * Factory for creating page objects with framework-aware locators.
 *
 * Each page receives the selected [App] so it can adapt locator strategies
 * through the unified `BasePage`/`BaseScreen` infrastructure.
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
    ): AbstractLoginPage = LoginPage(driver, platform, app, metricsManager)
    
    /**
     * Create a RegisterPage instance based on the app type.
     */
    fun createRegisterPage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractRegisterPage = RegisterPage(driver, platform, app, metricsManager)
    
    /**
     * Create a TasksPage instance based on the app type.
     */
    fun createTasksPage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractTasksPage = TasksPage(driver, platform, app, metricsManager)
    
    /**
     * Create a TaskCreatePage instance based on the app type.
     */
    fun createTaskCreatePage(
        driver: WebDriver,
        platform: Platform,
        app: App,
        metricsManager: MetricsManager? = null
    ): AbstractTaskCreatePage = TaskCreatePage(driver, platform, app, metricsManager)
}


