package com.danioliveira.appium.new

import com.danioliveira.appium.config.App
import com.danioliveira.appium.config.App.*
import com.danioliveira.appium.config.Platform
import com.danioliveira.appium.config.Platform.*
import com.danioliveira.appium.locators.Tags
import com.danioliveira.appium.metrics.MetricsManager
import com.danioliveira.appium.new.BaseScreen.Context.driver
import io.appium.java_client.AppiumBy
import io.appium.java_client.TouchAction
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.appium.java_client.ios.IOSDriver
import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.PointerInput
import org.openqa.selenium.interactions.Sequence
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

open class BaseScreen {

    protected val driver: WebDriver
        get() = Context.driver

    protected val platform: Platform
        get() = Context.platform

    protected val app: App
        get() = Context.app

    protected val metricsManager: MetricsManager?
        get() = Context.metricsManager

    inline fun <reified T : BaseScreen> on(verifyScreen: Boolean = true): T {
        val screen = T::class.java.getDeclaredConstructor().newInstance()
        if (verifyScreen) {
            screen.verify()
        }
        return screen
    }

    companion object {
        inline fun <reified T : BaseScreen> on(verifyScreen: Boolean = true): T {
            val screen = T::class.java.getDeclaredConstructor().newInstance()
            if (verifyScreen) {
                screen.verify()
            }
            return screen
        }
    }

    object Context {
        private val driverThreadLocal = ThreadLocal<WebDriver>()
        private val platformThreadLocal = ThreadLocal<Platform>()
        private val appThreadLocal = ThreadLocal<App>()
        private val metricsManagerThreadLocal = ThreadLocal<MetricsManager?>()

        var driver: WebDriver
            get() = driverThreadLocal.get()
                ?: throw IllegalStateException("Driver not initialized in Context")
            set(value) = driverThreadLocal.set(value)

        var platform: Platform
            get() = platformThreadLocal.get()
                ?: throw IllegalStateException("Platform not initialized in Context")
            set(value) = platformThreadLocal.set(value)

        var app: App
            get() = appThreadLocal.get()
                ?: throw IllegalStateException("App not initialized in Context")
            set(value) = appThreadLocal.set(value)

        var metricsManager: MetricsManager?
            get() = metricsManagerThreadLocal.get()
            set(value) = metricsManagerThreadLocal.set(value)

        fun init(
            driver: WebDriver,
            platform: Platform,
            app: App,
            metricsManager: MetricsManager? = null
        ) {
            this.driver = driver
            this.platform = platform
            this.app = app
            this.metricsManager = metricsManager
        }

        fun clear() {
            driverThreadLocal.remove()
            platformThreadLocal.remove()
            appThreadLocal.remove()
            metricsManagerThreadLocal.remove()
        }
    }

    inline fun <reified T : BaseScreen> doOnRepeat(times: Int, block: T.(Int) -> Unit): T {
        repeat(times) {
            block(this as T, it)
        }
        return this as T
    }


    open fun verify(): BaseScreen {
        return this
    }

    protected fun track(actionName: String, block: () -> Unit) {
        val pageName = this::class.simpleName ?: "UnknownPage"
        metricsManager?.trackAction(pageName, actionName, block) ?: block()
    }
}

fun getAndroidLocatorId(id: String, framework: App): By =
    when (framework) {
        App.FLUTTER -> AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"$id\")")
        App.KMM -> AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"$id\")")
    }

fun getLocatorId(id: String, platform: Platform): By =
    when (platform) {
        ANDROID -> getAndroidLocatorId(id, BaseScreen.Context.app)
        Platform.IOS -> getIOSLocatorId(id, BaseScreen.Context.app)
    }

fun getIOSLocatorId(id: String, framework: App): By =
    when (framework) {
        App.KMM -> AppiumBy.accessibilityId(id)
        App.FLUTTER -> AppiumBy.accessibilityId(id)
    }

fun WebElement.focusAndWriteText(text: String) {
    click()
    clear()
    sendKeys(text)
}

fun WebDriver.waitUntil(locator: By, timeout: Duration = Duration.ofSeconds(30)) {
    // println("Waiting for element: $locator")
    WebDriverWait(this, timeout).until {
        it.findElement(locator).isDisplayed
    }
}

fun WebDriver.scrollDown(
    id: String,
    platform: Platform,
    app: App,
    target: String,
    timeout: Duration = Duration.ofSeconds(30)
) {
    require(id.isNotEmpty()) { "ID cannot be empty" }
    require(target.isNotEmpty()) { "Target text cannot be empty" }

    withTimeout(timeout) {
        val elementFound = runCatching {
            when (platform) {
                ANDROID -> when (app) {
                    KMM -> findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"$target\")")).isDisplayed
                    FLUTTER -> findElement(AppiumBy.androidUIAutomator("new UiSelector().description(\"$target\")")).isDisplayed
                }
                IOS -> findElement(AppiumBy.iOSNsPredicateString("label == \"$target\"")).isDisplayed
            }
        }.getOrDefault(false)

        if (elementFound) return@withTimeout true

        val size = manage().window().size
        val startX = size.width / 2
        val startY = (size.height * 0.8).toInt()
        val endY = (size.height * 0.2).toInt()


        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val swipe = Sequence(finger, 1)
        swipe.addAction(
            finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                startX,
                startY
            )
        )
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        swipe.addAction(
            finger.createPointerMove(
                Duration.ofMillis(500),
                PointerInput.Origin.viewport(),
                startX,
                endY
            )
        )
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        (this as RemoteWebDriver).perform(listOf(swipe))

//        when (platform) {
//            ANDROID -> findElement(
//                AppiumBy.androidUIAutomator(
//                    "new UiScrollable(new UiSelector().scrollable(true).resourceIdMatches(\".*$id\")).scrollForward()"
//                )
//            )
//
//            IOS -> (this as JavascriptExecutor).executeScript(
//                "mobile: scroll",
//                hashMapOf("direction" to "down")
//            )
//        }
        false
    }
}

fun withTimeout(timeout: Duration, block: () -> Boolean) {
    val startTime = System.currentTimeMillis()
    var shouldEnd = false
    while (!shouldEnd && (System.currentTimeMillis() - startTime) < timeout.toMillis()) {
        shouldEnd = block()
    }
}

fun WebDriver.scrollUp(
    id: String,
    platform: Platform,
    app: App,
    target: String,
    timeout: Duration = Duration.ofSeconds(30)
) {
    require(target.isNotEmpty()) { "Target text cannot be empty" }
    require(id.isNotEmpty()) { "ID cannot be empty" }

    withTimeout(timeout) {
        val elementFound = runCatching {
            when (platform) {
                ANDROID -> when (app) {
                    KMM -> findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"$target\")")).isDisplayed
                    FLUTTER -> findElement(AppiumBy.androidUIAutomator("new UiSelector().description(\"$target\")")).isDisplayed
                }
                IOS -> findElement(AppiumBy.iOSNsPredicateString("label == \"$target\"")).isDisplayed
            }
        }.getOrDefault(false)

        if (elementFound) return@withTimeout true

        val size = manage().window().size
        val startX = size.width / 2
        val startY = (size.height * 0.2).toInt()
        val endY = (size.height * 0.8).toInt()

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val swipe = Sequence(finger, 1)
        swipe.addAction(
            finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                startX,
                startY
            )
        )
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        swipe.addAction(
            finger.createPointerMove(
                Duration.ofMillis(500),
                PointerInput.Origin.viewport(),
                startX,
                endY
            )
        )
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        (this as RemoteWebDriver).perform(listOf(swipe))

//        when (platform) {
//            ANDROID -> findElement(
//                AppiumBy
//                    .androidUIAutomator(
//                        "new UiScrollable(new UiSelector().scrollable(true).resourceIdMatches(\".*$id\"))" +
//                                ".scrollBackward()"
//                    )
//            )
//
//            IOS -> {
//                // Simple scroll up attempt for iOS
//                val params = HashMap<String, String>()
//                params["direction"] = "up"
//                (this as JavascriptExecutor).executeScript("mobile: scroll", params)
//            }
//        }
        false
    }
}


fun getLocatorByInstance(type: String, instance: Int): By =
    AppiumBy.androidUIAutomator("new UiSelector().className(\"$type\").instance($instance)")

fun getEditTextByInstance(instance: Int): By =
    getLocatorByInstance("android.widget.EditText", instance)


class TaskCreatePage : BaseScreen() {

    private val taskTitleLocator = when (app) {
        KMM -> when (platform) {
            Platform.ANDROID -> getEditTextByInstance(0)
            Platform.IOS -> getLocatorId(Tags.TXT_TASK_TITLE, platform)
        }

        FLUTTER -> getLocatorId(Tags.TXT_TASK_TITLE, platform)
    }

    private val descriptionLocator = when (app) {
        KMM -> when (platform) {
            Platform.ANDROID -> getEditTextByInstance(1)
            Platform.IOS -> getLocatorId(Tags.TXT_TASK_DESCRIPTION, platform)

        }

        FLUTTER -> getLocatorId(Tags.TXT_TASK_DESCRIPTION, platform)
    }

    private val saveButtonLocator = when (app) {
        KMM -> when (platform) {
            Platform.ANDROID -> getLocatorByInstance("android.widget.Button", 3)
            Platform.IOS -> getLocatorId(Tags.BTN_CREATE_TASK, platform)
        }

        FLUTTER -> getLocatorId(Tags.BTN_CREATE_TASK, platform)
    }

    override fun verify(): BaseScreen {
        driver.waitUntil(saveButtonLocator)
        return this
    }

    fun enterTitle(title: String): TaskCreatePage {
        track("enterTitle") {
            driver.findElement(taskTitleLocator).focusAndWriteText(title)
        }
        return this
    }

    fun enterDescription(description: String?): TaskCreatePage {
        description?.let {
            track("enterDescription") {
                driver.findElement(descriptionLocator).focusAndWriteText(description)
            }
        }
        return this
    }

    fun clickSave(): TasksPage {
        track("clickSave") {
            if (platform == Platform.ANDROID)
                (driver as AndroidDriver).pressKey(KeyEvent(AndroidKey.BACK))
            verify()
            driver.findElement(saveButtonLocator).click()
        }
        return on<TasksPage>()
    }

    fun createTask(title: String, description: String? = null): TasksPage {
        return enterTitle(title)
            .enterDescription(description)
            .clickSave()
    }
}


class TasksPage : BaseScreen() {

    private val taskListLocator = getLocatorId(Tags.LIST_TASKS, platform)

    private val addTaskButtonLocator = getLocatorId(Tags.BTN_ADD_TASK, platform)

    override fun verify(): TasksPage {
        driver.waitUntil(taskListLocator)
        driver.waitUntil(addTaskButtonLocator)
        return this
    }

    fun clickAddTask(): TaskCreatePage {
        track("clickAddTask") {
            driver.findElement(addTaskButtonLocator).click()
        }
        return on<TaskCreatePage>()
    }

    fun scrollDownToTask(taskName: String): TasksPage {
        track("scrollToBottom") {
            driver.scrollDown(Tags.LIST_TASKS, platform, app, taskName)
        }
        return this
    }

    fun scrollUpToTask(taskName: String): TasksPage {
        track("scrollToTop") {
            driver.scrollUp(Tags.LIST_TASKS, platform, app,taskName)
        }
        return this
    }

    fun clickProfile(): ProfileScreen {
        track("clickProfile") {
            driver.findElement(getLocatorId(Tags.BTN_PROFILE, platform)).click()
        }
        return on<ProfileScreen>()
    }
}

class ProfileScreen : BaseScreen() {
    private val logoutButtonLocator = getLocatorId(Tags.BTN_LOGOUT, platform)

    override fun verify(): ProfileScreen {
        driver.waitUntil(logoutButtonLocator)
        return this
    }

    fun clickLogout(): LoginPage {
        track("clickLogout") {
            driver.findElement(logoutButtonLocator).click()
            
            // Handle confirmation dialog
            if (platform == Platform.ANDROID) {
                // Wait for dialog and click Logout
                driver.waitUntil(AppiumBy.androidUIAutomator("new UiSelector().text(\"Logout\")"))
                driver.findElement(AppiumBy.androidUIAutomator("new UiSelector().text(\"Logout\")")).click()
            } else {
                // iOS handling (assuming standard alert)
                driver.switchTo().alert().accept()
            }
        }
        return on<LoginPage>()
    }
}

class RegisterPage : BaseScreen() {

    private val nameInputLocator = getLocatorId(Tags.TXT_NAME, platform)
    private val emailInputLocator = getLocatorId(Tags.TXT_EMAIL, platform)
    private val passwordLocator = getLocatorId(Tags.TXT_PASSWORD, platform)
    private val confirmPasswordLocator = getLocatorId(Tags.TXT_CONFIRM_PASSWORD, platform)
    private val registerButtonLocator = getLocatorId(Tags.BTN_REGISTER, platform)
    private val loginLinkLocator = getLocatorId(Tags.LINK_REGISTER, platform)

    override fun verify(): BaseScreen {
        driver.waitUntil(nameInputLocator)
        return this
    }

    fun enterName(name: String): RegisterPage {
        track("enterName") {
            driver.findElement(nameInputLocator).focusAndWriteText(name)
        }
        return this
    }

    fun enterEmail(email: String): RegisterPage {
        track("enterEmail") {
            driver.findElement(emailInputLocator).focusAndWriteText(email)
        }
        return this
    }

    fun enterPassword(password: String): RegisterPage {
        track("enterPassword") {
            driver.findElement(passwordLocator).focusAndWriteText(password)
        }
        return this
    }

    fun enterConfirmPassword(confirmPassword: String): RegisterPage {
        track("enterConfirmPassword") {
            driver.findElement(confirmPasswordLocator).focusAndWriteText(confirmPassword)
        }
        return this
    }

    fun hideKeyboard() {
        try {
            if (platform == Platform.ANDROID) {
                (driver as AndroidDriver).hideKeyboard()
            } else {
                driver.findElement(AppiumBy.accessibilityId("Done")).click()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun clickRegister(): TasksPage {
        track("clickRegister") {
            hideKeyboard()
            driver.findElement(registerButtonLocator).click()
        }
        return on<TasksPage>()
    }

    fun clickLoginLink(): LoginPage {
        track("clickLoginLink") {
            driver.findElement(loginLinkLocator).click()
        }
        return on<LoginPage>()
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): TasksPage {
        return enterName(name)
            .enterEmail(email)
            .enterPassword(password)
            .enterConfirmPassword(confirmPassword)
            .clickRegister()
    }

}


class LoginPage : BaseScreen() {

    private val loginButtonLocator = getLocatorId(Tags.BTN_LOGIN, platform)
    private val emailInputLocator = getLocatorId(Tags.TXT_EMAIL, platform)
    private val passwordLocator = getLocatorId(Tags.TXT_PASSWORD, platform)
    private val registerLinkLocator = getLocatorId(Tags.LINK_REGISTER, platform)


    override fun verify(): LoginPage {
        driver.waitUntil(loginButtonLocator)
        return this
    }

    fun enterEmail(email: String): LoginPage {
        track("enterEmail") {
            driver.findElement(emailInputLocator).focusAndWriteText(email)
        }
        return this
    }

    fun enterPassword(password: String): LoginPage {
        track("enterPassword") {
            driver.findElement(passwordLocator).focusAndWriteText(password)
        }
        return this
    }

    fun clickRegisterLink(): RegisterPage {
        track("clickRegisterLink") {
            driver.findElement(registerLinkLocator).click()
        }
        return on<RegisterPage>()
    }

    fun clickLogin(): TasksPage {
        track("clickLogin") {
            driver.findElement(loginButtonLocator).click()
        }
        return on<TasksPage>()
    }

    fun login(email: String, password: String): TasksPage {
        return enterEmail(email)
            .enterPassword(password)
            .clickLogin()
    }
}