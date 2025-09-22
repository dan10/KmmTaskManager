package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.ProjectAssignRequest
import com.danioliveira.taskmanager.api.routes.Projects
import com.danioliveira.taskmanager.domain.service.ProjectService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

/**
 * Routes for project member management following the pattern:
 * GET /v1/projects/{projectId}/users - Get all members in a project
 * POST /v1/projects/{projectId}/assign - Add member to project
 * DELETE /v1/projects/{projectId}/assign/{userId} - Remove member from project
 */
fun Route.projectMemberRoutes() {
    val projectService by inject<ProjectService>()

    authenticate("auth-jwt") {
        // Get users by project: GET /v1/projects/{projectId}/users
        get<Projects.Id.Users> { res ->
            val users = projectService.getUsersByProject(res.parent.projectId.toUUID())
            call.respond(users)
        }

        // Assign user to project: POST /v1/projects/{projectId}/assign
        post<Projects.Id.Assign> { res ->
            val request = call.receive<ProjectAssignRequest>()
            val assignment = projectService.assignUserToProject(
                projectId = res.parent.projectId.toUUID(), 
                userId = request.userId.toUUID(),
                creatorId = userPrincipal()
            )
            call.respond(HttpStatusCode.Created, assignment)
        }

        // Remove user from project: DELETE /v1/projects/{projectId}/assign/{userId}
        delete<Projects.Id.AssignUser> { res ->
            val removed = projectService.removeUserFromProject(
                projectId = res.parent.projectId.toUUID(),
                userId = res.userId.toUUID(),
                creatorId = userPrincipal()
            )
            if (removed) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}