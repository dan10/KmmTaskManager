package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.*
import com.danioliveira.taskmanager.domain.service.TaskService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.taskRoutes() {
    val service by inject<TaskService>()
    authenticate("auth-jwt") {
        // Project-specific task routes
        route("/tasks/projects/{projectId}") {
            // Get all tasks for a project
            get {
                val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                val tasks = service.findAll(projectId, page, size)
                call.respond(tasks)
            }

            // Create a new task for a project
            post {
                val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<ProjectTaskCreateRequest>()
                val principal = call.principal<JWTPrincipal>()
                val creatorId = principal?.payload?.getClaim("userId")?.asString() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized
                )

                // Convert ProjectTaskCreateRequest to TaskCreateRequest
                val taskRequest = TaskCreateRequest(
                    title = request.title,
                    description = request.description,
                    projectId = projectId,
                    assigneeId = request.assigneeId,
                    status = request.status,
                    dueDate = request.dueDate
                )

                service.create(taskRequest, creatorId)
                call.respond(HttpStatusCode.Created)
            }

            // Get a specific task from a project
            get("/task/{taskId}") {
                val taskId = call.parameters["taskId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                val task = service.findById(taskId)

                // Verify that the task belongs to the specified project
                if (task.projectId != projectId) {
                    return@get call.respond(HttpStatusCode.NotFound)
                }

                call.respond(task)
            }

            // Update a specific task in a project
            put("/task/{taskId}") {
                val taskId = call.parameters["taskId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val projectId = call.parameters["projectId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<ProjectTaskUpdateRequest>()

                // First check if the task exists and belongs to the project
                val existingTask = service.findById(taskId)

                if (existingTask.projectId != projectId) {
                    return@put call.respond(HttpStatusCode.NotFound)
                }

                // Convert ProjectTaskUpdateRequest to TaskUpdateRequest
                val taskRequest = TaskUpdateRequest(
                    title = request.title,
                    description = request.description,
                    status = request.status,
                    dueDate = request.dueDate,
                    assigneeId = request.assigneeId
                )

                val updated = service.update(taskId, taskRequest)
                call.respond(updated)
            }
        }

        route("/tasks") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized
                )
                val projectId = call.request.queryParameters["projectId"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val assigneeId = call.request.queryParameters["assigneeId"]

                val tasks = when {
                    assigneeId != null -> service.findAllByAssigneeId(assigneeId, page, size)
                    projectId != null -> service.findAll(
                        projectId,
                        page,
                        size
                    ) // Keep backward compatibility for project ID query parameter
                    else -> service.findAllByAssigneeId(userId, page, size) // Get all tasks where user is assignee
                }

                call.respond(tasks)
            }

            get("/user") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized
                )
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                val tasks = service.findAllByOwnerId(userId, page, size)
                call.respond(tasks)
            }

            post("/user") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized
                )

                // Receive the request but modify it to set the current user as assignee
                val requestData = call.receive<TaskCreateRequest>()
                val request = requestData.copy(assigneeId = userId)

                val task = service.create(request, userId)
                call.respond(task)
            }
            post {
                val request = call.receive<TaskCreateRequest>()
                val principal = call.principal<JWTPrincipal>()
                val creatorId = principal?.payload?.getClaim("userId")?.asString() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized
                )
                val task = service.create(request, creatorId)
                call.respond(task)
            }
            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val task = service.findById(id)
                call.respond(task)
            }
            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<TaskUpdateRequest>()
                val updated = service.update(id, request)
                call.respond(updated)
            }
            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val deleted = service.delete(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post("/{id}/assign") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<TaskAssignRequest>()
                val updated = service.assign(id, request.assigneeId)
                if (updated != null) {
                    call.respond(updated)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            post("/{id}/status") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<TaskStatusChangeRequest>()
                val updated = service.changeStatus(id, request.status)
                if (updated != null) {
                    call.respond(updated)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
