package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.ProjectCreateRequest
import com.danioliveira.taskmanager.api.request.ProjectUpdateRequest
import com.danioliveira.taskmanager.api.routes.Projects
import com.danioliveira.taskmanager.api.routes.UserProjects
import com.danioliveira.taskmanager.auth.UserPrincipal
import com.danioliveira.taskmanager.domain.exceptions.UnauthorizedException
import com.danioliveira.taskmanager.domain.service.ProjectService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.projectRoutes() {
    val service by inject<ProjectService>()

    authenticate("auth-jwt") {
        get<UserProjects> { res ->
            val userId = userPrincipal()

            val projects = service.getProjectsByOwner(
                ownerId = userId,
                page = res.page,
                size = res.size,
                query = res.searchText
            )

            call.respond(projects)
        }

        get<Projects.Id> {
            val userId = userPrincipal()
            val projectUuid = UUID.fromString(it.projectId)

            val project = service.getProjectById(id = projectUuid, userId)
            call.respond(project)
        }

        put<Projects.Id> { res ->
            val userId = userPrincipal()
            val request = call.receive<ProjectUpdateRequest>()
            val updated = service.updateProject(res.projectId.toUUID(), userId, request)
            if (updated) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Create project: POST v1/projects
        post<Projects> {
            val userId = userPrincipal()
            val req = call.receive<ProjectCreateRequest>()
            val project = service.createProject(userId, req)
            call.respond(project)
        }

        // Delete project by id: DELETE v1/projects/{projectId}
        delete<Projects.Id> { res ->
            val userId = userPrincipal()
            val deleted = service.deleteProject(res.projectId.toUUID(), userId)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

    }
}

fun RoutingContext.userPrincipal(): UUID {
    return call.principal<UserPrincipal>()?.userId ?: throw UnauthorizedException()
}

fun String.toUUID(): UUID = try {
    UUID.fromString(this)
} catch (_: IllegalArgumentException) {
    throw UnauthorizedException("Invalid UUID format")
}
