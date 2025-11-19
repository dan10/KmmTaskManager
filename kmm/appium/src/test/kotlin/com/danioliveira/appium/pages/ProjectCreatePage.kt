package com.danioliveira.appium.pages

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import org.openqa.selenium.WebDriver

/**
 * Page Object for Project Creation form.
 * Handles interactions with the project creation bottom sheet/modal.
 */
class ProjectCreatePage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : BasePage(driver, platform, metricsManager) {

    /**
     * Wait for the project creation form to be visible.
     * Waits for the name field to appear as an indicator.
     */
    fun waitForCreateForm() {
        waitForElement(Tags.TXT_PROJECT_NAME)
    }

    /**
     * Enter project name.
     * @param name The project name to enter
     */
    fun enterName(name: String) {
        sendKeysById(Tags.TXT_PROJECT_NAME, name)
    }

    /**
     * Enter project description.
     * @param description The project description to enter
     */
    fun enterDescription(description: String) {
        sendKeysById(Tags.TXT_PROJECT_DESCRIPTION, description)
    }

    /**
     * Click the save button to create the project.
     */
    fun clickSave() {
        clickById(Tags.BTN_SAVE)
    }

    /**
     * Click the cancel button to dismiss the form.
     */
    fun clickCancel() {
        clickById(Tags.BTN_CANCEL)
    }

    /**
     * Complete flow: Enter name, description (optional), and save.
     * @param name Required project name
     * @param description Optional project description
     */
    fun createProject(name: String, description: String? = null) {
        trackAction("createProject") {
            waitForCreateForm()
            enterName(name)
            if (description != null) {
                enterDescription(description)
            }
            clickSave()
        }
    }

    /**
     * Quick create: Just name and save (for bulk creation).
     * @param name The project name
     */
    fun quickCreate(name: String) {
        trackAction("quickCreateProject") {
            enterName(name)
            clickSave()
        }
    }
}