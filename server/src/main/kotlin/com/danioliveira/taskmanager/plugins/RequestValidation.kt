package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.api.request.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

/**
 * Configures the RequestValidation plugin to validate request models.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        // Register validation
        validate<RegisterRequest> { request ->
            val errors = mutableMapOf<String, String>()

            if (request.email.isBlank()) {
                errors["email"] = "Email cannot be empty"
            } else if (!request.email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
                errors["email"] = "Invalid email format"
            }

            if (request.password.isBlank()) {
                errors["password"] = "Password cannot be empty"
            } else if (request.password.length < 8) {
                errors["password"] = "Password must be at least 8 characters long"
            }

            if (request.displayName.isBlank()) {
                errors["displayName"] = "Display name cannot be empty"
            }

            if (errors.isNotEmpty()) {
                return@validate ValidationResult.Invalid(errors.map { it.key to it.value }
                    .joinToString(", ") { "${it.first}: ${it.second}" })
            }

            ValidationResult.Valid
        }

        // Login validation
        validate<LoginRequest> { request ->
            val errors = mutableMapOf<String, String>()

            if (request.email.isBlank()) {
                errors["email"] = "Email cannot be empty"
            } else if (!request.email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))) {
                errors["email"] = "Invalid email format"
            }

            if (request.password.isBlank()) {
                errors["password"] = "Password cannot be empty"
            }

            if (errors.isNotEmpty()) {
                return@validate ValidationResult.Invalid(errors.map { it.key to it.value }
                    .joinToString(", ") { "${it.first}: ${it.second}" })
            }

            ValidationResult.Valid
        }

        // Google login validation
        validate<GoogleLoginRequest> { request ->
            if (request.idToken.isBlank()) {
                return@validate ValidationResult.Invalid("ID token cannot be empty")
            }
            ValidationResult.Valid
        }

        // Project create validation
        validate<ProjectCreateRequest> { request ->
            if (request.name.isBlank()) {
                return@validate ValidationResult.Invalid("Project name cannot be empty")
            }
            ValidationResult.Valid
        }

        // Project update validation
        validate<ProjectUpdateRequest> { request ->
            if (request.name.isBlank()) {
                return@validate ValidationResult.Invalid("Project name cannot be empty if provided")
            }
            ValidationResult.Valid
        }

        // Task create validation
        validate<TaskCreateRequest> { request ->
            val errors = mutableMapOf<String, String>()

            if (request.title.isBlank()) {
                errors["title"] = "Task title cannot be empty"
            }

            if (request.assigneeId.isBlank()) {
                errors["assigneeId"] = "Assignee ID cannot be empty"
            }

            if (request.status.isBlank()) {
                errors["status"] = "Status cannot be empty"
            }

            if (errors.isNotEmpty()) {
                return@validate ValidationResult.Invalid(errors.map { it.key to it.value }
                    .joinToString(", ") { "${it.first}: ${it.second}" })
            }

            ValidationResult.Valid
        }

        // Task update validation
        validate<TaskUpdateRequest> { request ->
            if (request.title?.isBlank() == true) {
                return@validate ValidationResult.Invalid("Task title cannot be empty if provided")
            }
            ValidationResult.Valid
        }

        // Task status change validation
        validate<TaskStatusChangeRequest> { request ->
            if (request.status.isBlank()) {
                return@validate ValidationResult.Invalid("Status cannot be empty")
            }
            ValidationResult.Valid
        }

        // Task assign validation
        validate<TaskAssignRequest> { request ->
            if (request.assigneeId.isBlank()) {
                return@validate ValidationResult.Invalid("Assignee ID cannot be empty")
            }
            ValidationResult.Valid
        }

        // Pagination validation
        validate<PaginationRequest> { request ->
            val errors = mutableMapOf<String, String>()

            if (request.page < 0) {
                errors["page"] = "Page number cannot be negative"
            }

            if (request.size <= 0) {
                errors["size"] = "Size must be greater than 0"
            } else if (request.size > 100) {
                errors["size"] = "Size cannot be greater than 100"
            }

            if (errors.isNotEmpty()) {
                return@validate ValidationResult.Invalid(errors.map { it.key to it.value }
                    .joinToString(", ") { "${it.first}: ${it.second}" })
            }

            ValidationResult.Valid
        }
    }
}