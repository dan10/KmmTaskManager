package com.danioliveira.appium.locators

/**
 * Shared test identifiers matching TestTags.kt (Compose) and TestIds.dart (Flutter).
 */
object Tags {
    // Auth
    const val BTN_LOGIN = "btn_login"
    const val BTN_REGISTER = "btn_register"
    const val BTN_LOGOUT = "btn_logout"
    const val TXT_NAME = "txt_name"
    const val TXT_EMAIL = "txt_email"
    const val TXT_PASSWORD = "txt_password"
    const val TXT_CONFIRM_PASSWORD = "txt_confirm_password"
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
    
    fun taskCard(id: String): String = "$CARD_TASK$id"
    fun projectCard(id: String): String = "$CARD_PROJECT$id"
}


