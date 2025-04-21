package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.domain.service.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Defines the authentication routes for the application.
 */
fun Route.authRoutes() {
    val userService: UserService by inject()

    route("/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val result = userService.register(request)

                if (result == null) {
                    call.respond(HttpStatusCode.Conflict, "Email already registered")
                    return@post
                }

                val (token, user) = result
                call.respond(mapOf("token" to token, "user" to user))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid registration data: ${e.message}")
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val result = userService.login(request)

                if (result == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@post
                }

                val (token, user) = result
                call.respond(mapOf("token" to token, "user" to user))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid login data: ${e.message}")
            }
        }

        post("/google") {
            try {
                val request = call.receive<GoogleLoginRequest>()
                val result = userService.googleLogin(request)

                if (result == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid Google ID token")
                    return@post
                }

                val (token, user) = result
                call.respond(mapOf("token" to token, "user" to user))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid Google login data: ${e.message}")
            }
        }
    }
}
