package com.danioliveira.taskmanager.testing

/**
 * Test tags for Appium automation.
 * These tags are exposed as resource-id (Android) and accessibilityIdentifier (iOS)
 * when testTagsAsResourceId is enabled on the root composable.
 */
object TestTags {
    // Auth
    const val BTN_LOGIN = "btn_login"
    const val BTN_REGISTER = "btn_register"
    const val BTN_LOGOUT = "btn_logout"
    const val TXT_EMAIL = "txt_email"
    const val TXT_PASSWORD = "txt_password"
    const val LINK_REGISTER = "link_register"
    
    // Navigation
    const val NAV_TASKS = "nav_tasks"
    const val NAV_PROJECTS = "nav_projects"
    const val NAV_CALENDAR = "nav_calendar"
    
    // Tasks
    const val BTN_ADD_TASK = "btn_add_task"
    const val LIST_TASKS = "list_tasks"
    const val TXT_SEARCH = "txt_search"
    const val CARD_TASK = "card_task_"
    
    // Projects
    const val BTN_ADD_PROJECT = "btn_add_project"
    const val LIST_PROJECTS = "list_projects"
    const val CARD_PROJECT = "card_project_"
    
    // Calendar
    const val LIST_CALENDAR_TASKS = "list_calendar_tasks"
    
    // Profile
    const val BTN_PROFILE = "btn_profile"
    
    // Common
    const val BTN_BACK = "btn_back"
    const val BTN_SAVE = "btn_save"
    const val BTN_CANCEL = "btn_cancel"
    
    /**
     * Generate a task card tag with task ID
     */
    fun taskCard(id: String): String = "$CARD_TASK$id"
    
    /**
     * Generate a project card tag with project ID
     */
    fun projectCard(id: String): String = "$CARD_PROJECT$id"
}





