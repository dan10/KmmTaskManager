package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.plugins.configureDI
import com.danioliveira.taskmanager.plugins.configureDatabase
import com.danioliveira.taskmanager.plugins.configureMetrics
import com.danioliveira.taskmanager.plugins.configureRequestValidation
import com.danioliveira.taskmanager.plugins.configureSecurity
import com.danioliveira.taskmanager.plugins.configureSerialization
import com.danioliveira.taskmanager.plugins.configureStatusPages
import com.danioliveira.taskmanager.routes.authRoutes
import com.danioliveira.taskmanager.routes.projectMemberRoutes
import com.danioliveira.taskmanager.routes.projectRoutes
import com.danioliveira.taskmanager.routes.projectTaskRoutes
import com.danioliveira.taskmanager.routes.taskRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.property
import io.ktor.server.netty.EngineMain
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    // Configure plugins
    configureDI()

    // Get AppConfig from Koin

    configureDatabase()
    JwtConfig.init(property<com.danioliveira.taskmanager.domain.JwtConfig>("ktor.jwt"))
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureRequestValidation()
    configureMetrics()
    install(Resources)

    routing {
        authRoutes()
        projectRoutes()
        projectTaskRoutes()
        projectMemberRoutes()
        taskRoutes()
    }
}
