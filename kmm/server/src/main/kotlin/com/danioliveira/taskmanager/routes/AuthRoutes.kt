package com.danioliveira.taskmanager.routes

import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.routes.Auth
import com.danioliveira.taskmanager.domain.service.UserService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Route handler for the nested `Auth` resource
 */
fun Route.authRoutes() {
    val userService: UserService by inject()

    post<Auth.Register> {
        val request = call.receive<RegisterRequest>()
        userService.register(request)
    }

    post<Auth.Login> {
        val request = call.receive<LoginRequest>()
        val authResponse = userService.login(request)
        call.respond(authResponse)
    }

    post<Auth.Google> {
        val request = call.receive<GoogleLoginRequest>()
        val authResponse = userService.googleLogin(request)
        call.respond(authResponse)
    }
}
