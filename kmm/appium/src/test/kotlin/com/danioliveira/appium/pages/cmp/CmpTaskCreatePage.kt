package com.danioliveira.appium.pages.cmp

import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.pages.abstract.AbstractTaskCreatePage
import com.danioliveira.appium.pages.abstract.AbstractTasksPage
import org.openqa.selenium.WebDriver

/**
 * CMP (Compose Multiplatform) implementation of TaskCreatePage.
 * Uses instance-based finding as fallback for bottom sheets.
 */
class CmpTaskCreatePage(
    driver: WebDriver,
    platform: Platform,
    metricsManager: MetricsManager? = null
) : AbstractTaskCreatePage(driver, platform, metricsManager) {
    
    override fun verify(): CmpTaskCreatePage {
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
        return CmpTasksPage(driver, platform, metricsManager)
    }
    
    override fun clickCancel(): AbstractTasksPage {
        clickById(Tags.BTN_CANCEL)
        return CmpTasksPage(driver, platform, metricsManager)
    }
    
    override fun quickCreateByInstance(title: String): AbstractTasksPage {
        return trackAction("quickCreateTask_instance") {
            val page = enterTitleByInstance(title)
            page.clickSaveByInstance()
        }
    }
    
    private fun waitForCreateFormByInstance(): CmpTaskCreatePage {
        trackAction("waitForCreateForm_instance") {
            try {
                findEditTextByInstance(0)
            } catch (e: Exception) {
                throw Exception("Task create form not visible - first EditText not found", e)
            }
        }
        return this
    }
    
    private fun enterTitleByInstance(title: String): CmpTaskCreatePage {
        sendKeysToEditTextByInstance(0, title)
        return this
    }
    
    private fun enterDescriptionByInstance(description: String): CmpTaskCreatePage {
        sendKeysToEditTextByInstance(1, description)
        return this
    }
    
    private fun clickSaveByInstance(): AbstractTasksPage {
        clickButtonByInstance(3)
        return CmpTasksPage(driver, platform, metricsManager)
    }
}

