package com.danioliveira.appium.pages.flutter

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * Flutter implementation of TaskCreatePage.
 * Uses instance-based finding as fallback for dialogs/bottom sheets.
 */
class FlutterTaskCreatePage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractTaskCreatePage(driver, platform, metricsManager) {
    
    override fun verify(): FlutterTaskCreatePage {
        // Try ID-based first, fall back to instance-based
        try {
            waitForElement(Tags.TXT_TASK_TITLE)
        } catch (e: Exception) {
            waitForCreateFormByInstance()
        }
        return this
    }
    
    override fun waitForCreateForm(): AbstractTaskCreatePage {
        try {
            waitForElement(Tags.TXT_TASK_TITLE)
        } catch (e: Exception) {
            waitForCreateFormByInstance()
        }
        return this
    }
    
    override fun enterTitle(title: String): AbstractTaskCreatePage {
        try {
            sendKeysById(Tags.TXT_TASK_TITLE, title)
        } catch (e: Exception) {
            enterTitleByInstance(title)
        }
        return this
    }
    
    override fun enterDescription(description: String): AbstractTaskCreatePage {
        try {
            sendKeysById(Tags.TXT_TASK_DESCRIPTION, description)
        } catch (e: Exception) {
            enterDescriptionByInstance(description)
        }
        return this
    }
    
    override fun clickSave(): AbstractTasksPage {
        try {
            clickById(Tags.BTN_SAVE)
        } catch (e: Exception) {
            clickSaveByInstance()
        }
        return FlutterTasksPage(driver, platform, metricsManager)
    }
    
    override fun clickCancel(): AbstractTasksPage {
        clickById(Tags.BTN_CANCEL)
        return FlutterTasksPage(driver, platform, metricsManager)
    }
    
    override fun quickCreateByInstance(title: String): AbstractTasksPage {
        return trackAction("quickCreateTask_instance") {
            val page = enterTitleByInstance(title)
            page.clickSaveByInstance()
        }
    }
    
    private fun waitForCreateFormByInstance(): FlutterTaskCreatePage {
        trackAction("waitForCreateForm_instance") {
            try {
                findEditTextByInstance(0)
            } catch (e: Exception) {
                throw Exception("Task create form not visible - first EditText not found", e)
            }
        }
        return this
    }
    
    private fun enterTitleByInstance(title: String): FlutterTaskCreatePage {
        sendKeysToEditTextByInstance(0, title)
        return this
    }
    
    private fun enterDescriptionByInstance(description: String): FlutterTaskCreatePage {
        sendKeysToEditTextByInstance(1, description)
        return this
    }
    
    private fun clickSaveByInstance(): AbstractTasksPage {
        clickButtonByInstance(3)
        return FlutterTasksPage(driver, platform, metricsManager)
    }
}

