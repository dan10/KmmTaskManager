package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.domain.service.ProjectService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class ProjectAssignRequest(val userId: String)

fun Route.projectRoutes() {
    val service by inject<ProjectService>()

    authenticate("auth-jwt") {
        route("/projects") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized
                )
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val projects = service.getProjectsByOwner(userId, page, size)
                call.respond(projects)
            }

            get("/all") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
                val projects = service.getAllProjects(page, size)
                call.respond(projects)
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized
                )
                val req = call.receive<ProjectCreateRequest>()
                val project = service.createProject(userId, req)
                call.respond(project)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val project = service.getProjectById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(project)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<ProjectUpdateRequest>()
                val updated = service.updateProject(id, req)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val deleted = service.deleteProject(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/{id}/assign") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<ProjectAssignRequest>()
                try {
                    val assignment = service.assignUserToProject(id, req.userId)
                    call.respond(assignment)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.Conflict, e.message ?: "User already assigned to project")
                }
            }

            delete("/{id}/assign/{userId}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val userId = call.parameters["userId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val removed = service.removeUserFromProject(id, userId)
                if (removed) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            get("/{id}/users") {
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val users = service.getUsersByProject(id)
                call.respond(users)
            }

            get("/user/{userId}") {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val projects = service.getProjectsByUser(userId)
                call.respond(projects)
            }
        }
    }
}
