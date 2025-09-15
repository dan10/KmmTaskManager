package com.danioliveira.taskmanager.api.routes

internal object Routes {

    const val ALL_USERS = "v1/users"

    const val BY_USER_ID = "{userId}"

    const val ALL_PROJECTS = "v1/projects"
    const val BY_PROJECT_ID = "{projectId}"

    const val ALL_TASKS = "v1/tasks"
    const val BY_TASK_ID = "{taskId}"

    const val ALL_PROJECT_ASSIGNMENTS = "v1/assignments"
    const val BY_PROJECT_ASSIGNMENT_ID = "{assignmentId}"

    // Nested/user-specific routes
    const val USER_PROJECTS = "v1/users/{userId}/projects"
    const val USER_TASKS = "v1/users/{userId}/tasks"

    const val PROJECT_TASKS = "v1/projects/{projectId}/tasks"

    // Auth routes
    const val AUTH = "v1/auth"

    // Child segments for auth (use in Resources)
    const val AUTH_CHILD_LOGIN = "login"
    const val AUTH_CHILD_GOOGLE = "login/google"
    const val AUTH_CHILD_REGISTER = "register"
    const val AUTH_CHILD_REGISTER_GOOGLE = "register/google"

    const val HEALTH_CHECK = "/health"
}