package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.domain.service.UserService
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
            val request = call.receive<RegisterRequest>()
            val authResponse = userService.register(request)
            call.respond(authResponse)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val authResponse = userService.login(request)
            call.respond(authResponse)
        }

        post("/google") {
            val request = call.receive<GoogleLoginRequest>()
            val authResponse = userService.googleLogin(request)
            call.respond(authResponse)
        }
    }
}
