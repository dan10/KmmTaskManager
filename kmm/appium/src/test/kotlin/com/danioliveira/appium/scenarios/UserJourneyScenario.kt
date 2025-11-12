package com.danioliveira.appium.scenarios

import com.danioliveira.appium.config.BenchmarkConfig
import com.danioliveira.appium.pages.*
import org.openqa.selenium.WebDriver
import org.slf4j.LoggerFactory
import java.util.*

class UserJourneyScenario(
    private val driver: WebDriver,
    private val config: BenchmarkConfig
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun executeCycle(cycleNumber: Int) {
        logger.info("Starting cycle $cycleNumber")
        
        val loginPage = LoginPage(driver, config.platform)
        val navPage = NavigationPage(driver, config.platform)
        val tasksPage = TasksPage(driver, config.platform)
        val projectsPage = ProjectsPage(driver, config.platform)
        val calendarPage = CalendarPage(driver, config.platform)
        val profilePage = ProfilePage(driver, config.platform)
        
        // Register (only first cycle or if registerEveryCycle is true)
        if (cycleNumber == 1 || config.registerEveryCycle) {
            register(loginPage)
        }
        
        // Login
        login(loginPage)
        
        // Create tasks
        createTasks(tasksPage, navPage)
        
        // View calendar
        viewCalendar(calendarPage, navPage)
        
        // Create project
        val projectId = createProject(projectsPage, navPage)
        
        // Go to project details and create tasks
        if (projectId != null) {
            goToProjectDetails(projectsPage, projectId)
            createTasksInProject(tasksPage)
            goBack(driver)
        }
        
        // Logout
        logout(profilePage, navPage)
        
        // Login again
        login(loginPage)
        
        // See list and scroll to end
        seeListAndScroll(tasksPage, navPage)
        
        // Logout
        logout(profilePage, navPage)
        
        logger.info("Completed cycle $cycleNumber")
    }
    
    private fun register(loginPage: LoginPage) {
        logger.info("Registering user")
        loginPage.waitForLoginScreen()
        loginPage.clickRegisterLink()
        Thread.sleep(1000)
        // Fill registration form (simplified - adjust based on actual UI)
        // For now, assume registration is handled manually or via API
        Thread.sleep(2000)
    }
    
    private fun login(loginPage: LoginPage) {
        logger.info("Logging in")
        loginPage.waitForLoginScreen()
        loginPage.enterEmail("test@example.com")
        loginPage.enterPassword("password123")
        loginPage.clickLogin()
        Thread.sleep(3000) // Wait for login to complete
    }
    
    private fun createTasks(tasksPage: TasksPage, navPage: NavigationPage) {
        logger.info("Creating tasks")
        navPage.navigateToTasks()
        tasksPage.waitForTasksList()
        Thread.sleep(1000)
        
        // Create a few tasks (simplified - adjust based on actual UI)
        repeat(2) {
            tasksPage.clickAddTask()
            Thread.sleep(1000)
            // Fill task form and save (simplified)
            // driver.findElement(By.id(Tags.BTN_SAVE)).click()
            Thread.sleep(1000)
        }
    }
    
    private fun viewCalendar(calendarPage: CalendarPage, navPage: NavigationPage) {
        logger.info("Viewing calendar")
        navPage.navigateToCalendar()
        calendarPage.waitForCalendarList()
        Thread.sleep(2000)
    }
    
    private fun createProject(projectsPage: ProjectsPage, navPage: NavigationPage): String? {
        logger.info("Creating project")
        navPage.navigateToProjects()
        projectsPage.waitForProjectsList()
        Thread.sleep(1000)
        
        projectsPage.clickAddProject()
        Thread.sleep(1000)
        // Fill project form and save (simplified)
        // driver.findElement(By.id(Tags.BTN_SAVE)).click()
        Thread.sleep(1000)
        
        // Return a mock project ID - in real scenario, extract from UI
        return UUID.randomUUID().toString()
    }
    
    private fun goToProjectDetails(projectsPage: ProjectsPage, projectId: String) {
        logger.info("Going to project details")
        projectsPage.clickProject(projectId)
        Thread.sleep(2000)
    }
    
    private fun createTasksInProject(tasksPage: TasksPage) {
        logger.info("Creating tasks in project")
        tasksPage.waitForTasksList()
        Thread.sleep(1000)
        
        repeat(2) {
            tasksPage.clickAddTask()
            Thread.sleep(1000)
            // Fill task form and save (simplified)
            Thread.sleep(1000)
        }
    }
    
    private fun goBack(driver: WebDriver) {
        logger.info("Going back")
        driver.navigate().back()
        Thread.sleep(1000)
    }
    
    private fun seeListAndScroll(tasksPage: TasksPage, navPage: NavigationPage) {
        logger.info("Viewing list and scrolling")
        navPage.navigateToTasks()
        tasksPage.waitForTasksList()
        Thread.sleep(1000)
        tasksPage.scrollToEnd()
        Thread.sleep(2000)
    }
    
    private fun logout(profilePage: ProfilePage, navPage: NavigationPage) {
        logger.info("Logging out")
        // Navigate to profile (simplified - adjust based on actual UI)
        // navPage.clickProfile() or similar
        profilePage.waitForProfileScreen()
        profilePage.clickLogout()
        Thread.sleep(2000)
    }
}

