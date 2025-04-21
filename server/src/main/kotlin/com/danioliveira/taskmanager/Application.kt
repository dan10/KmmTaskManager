package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.plugins.*
import com.danioliveira.taskmanager.routes.authRoutes
import com.danioliveira.taskmanager.routes.projectRoutes
import com.danioliveira.taskmanager.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configure plugins
    configureDI()

    // Get AppConfig from Koin
    val appConfig by inject<AppConfig>()

    configureDatabase(appConfig)
    JwtConfig.init(appConfig)
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureRequestValidation()

    routing {
        authRoutes()
        projectRoutes()
        taskRoutes()
    }
}
