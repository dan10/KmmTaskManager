package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.TaskAssignRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskStatusChangeRequest
import com.danioliveira.taskmanager.api.request.TaskUpdateRequest
import com.danioliveira.taskmanager.api.routes.Tasks
import com.danioliveira.taskmanager.api.routes.TasksPaginated
import com.danioliveira.taskmanager.domain.service.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.resources.delete
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

/**
 * Routes for task management following Routes.md specification:
 * 
 * Basic Task Operations:
 * GET /v1/tasks - Get all tasks for authenticated user
 * POST /v1/tasks - Create new task
 * GET /v1/tasks/{id} - Get specific task details
 * PUT /v1/tasks/{id} - Update task
 * DELETE /v1/tasks/{id} - Delete task
 * PATCH /v1/tasks/{id}/status - Update task status (todo/doing/completed)
 * 
 * Task Filtering and Stats:
 * GET /v1/tasks/owned - Get tasks owned by user
 * GET /v1/tasks/assigned - Get tasks assigned to user
 * GET /v1/tasks/stats - Get task statistics (counts by status)
 * 
 * Task Assignment:
 * POST /v1/tasks/{id}/assign - Assign task to user
 */
fun Route.taskRoutes() {
    val taskService by inject<TaskService>()

    authenticate("auth-jwt") {
        
        // Get tasks with pagination and filtering: GET /v1/tasks
        get<TasksPaginated> { res ->
            val userId = userPrincipal().toString()
            val tasks = when {
                res.searchText != null -> taskService.findAllByAssigneeId(userId, res.page ?: 0, res.size, res.searchText)
                else -> taskService.findAllByAssigneeId(userId, res.page ?: 0, res.size, null)
            }
            call.respond(tasks)
        }

        // Create new task: POST /v1/tasks
        post<Tasks> {
            val userId = userPrincipal().toString()
            val request = call.receive<TaskCreateRequest>()
            val task = taskService.create(request, userId)
            call.respond(HttpStatusCode.Created, task)
        }

        // Get specific task: GET /v1/tasks/{taskId}
        get<Tasks.Id> { res ->
            val task = taskService.findById(res.taskId)
            call.respond(task)
        }

        // Update task: PUT /v1/tasks/{taskId}
        put<Tasks.Id> { res ->
            val request = call.receive<TaskUpdateRequest>()
            val updated = taskService.update(res.taskId, request)
            call.respond(updated)
        }

        // Delete task: DELETE /v1/tasks/{taskId}
        delete<Tasks.Id> { res ->
            val deleted = taskService.delete(res.taskId)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Task filtering and stats routes using Resources (following Routes.md)
        
        // Get tasks owned by current user: GET /v1/tasks/owned
        get<Tasks.Owned> { res ->
            val userId = userPrincipal().toString()
            val tasks = taskService.findAllByOwnerId(userId, res.page, res.size)
            call.respond(tasks)
        }

        // Get tasks assigned to current user: GET /v1/tasks/assigned
        get<Tasks.Assigned> { res ->
            val userId = userPrincipal().toString()
            val tasks = taskService.findAllByAssigneeId(userId, res.page, res.size, res.query)
            call.respond(tasks)
        }

        // Get task statistics: GET /v1/tasks/stats
        get<Tasks.Stats> {
            val userId = userPrincipal().toString()
            val stats = taskService.getUserTaskProgress(userId)
            call.respond(stats)
        }

        // Task action endpoints using Resources
        
        // Assign task to user: POST /v1/tasks/{taskId}/assign
        post<Tasks.Id.Assign> { res ->
            val request = call.receive<TaskAssignRequest>()
            val updated = taskService.assign(res.parent.taskId, request.assigneeId)
            call.respond(updated)
        }

        // Change task status: POST /v1/tasks/{taskId}/status
        post<Tasks.Id.Status> { res ->
            val request = call.receive<TaskStatusChangeRequest>()
            val updated = taskService.changeStatus(res.parent.taskId, request.status)
            call.respond(updated)
        }
    }
}