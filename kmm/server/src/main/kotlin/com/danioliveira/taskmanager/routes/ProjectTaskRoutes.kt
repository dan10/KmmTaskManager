package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.ProjectTaskCreateRequest
import com.danioliveira.taskmanager.api.request.TaskCreateRequest
import com.danioliveira.taskmanager.api.routes.Projects
import com.danioliveira.taskmanager.domain.service.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

/**
 * Routes for project-task relationships following the pattern:
 * GET /v1/projects/{projectId}/tasks - Get all tasks in a project
 * POST /v1/projects/{projectId}/tasks - Create task in a project
 * PUT /v1/projects/{projectId}/tasks/{taskId} - Update task in a project
 */
fun Route.projectTaskRoutes() {
    val taskService by inject<TaskService>()

    authenticate("auth-jwt") {
        // Get all tasks for a project: GET /v1/projects/{projectId}/tasks
        get<Projects.Id.Tasks> { res ->
            val projectId = res.parent.projectId
            val tasks = taskService.findAllByProjectId(
                projectId = projectId,
                page = res.page,
                size = res.size
            )
            call.respond(tasks)
        }

        // Create task in a project: POST /v1/projects/{projectId}/tasks
        post<Projects.Id.Tasks> { res ->
            val projectId = res.parent.projectId
            val request = call.receive<ProjectTaskCreateRequest>()
            val creatorId = userPrincipal().toString()

            // Convert ProjectTaskCreateRequest to TaskCreateRequest
            val taskRequest = TaskCreateRequest(
                title = request.title,
                description = request.description,
                projectId = projectId,
                assigneeId = request.assigneeId,
                priority = request.priority,
                dueDate = request.dueDate
            )

            val task = taskService.create(taskRequest, creatorId)
            call.respond(HttpStatusCode.Created, task)
        }
    }
}