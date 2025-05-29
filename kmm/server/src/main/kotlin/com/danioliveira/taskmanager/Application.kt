package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.plugins.*
import com.danioliveira.taskmanager.routes.adminRoutes
import com.danioliveira.taskmanager.routes.authRoutes
import com.danioliveira.taskmanager.routes.projectRoutes
import com.danioliveira.taskmanager.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

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
    configureMetrics()

    routing {
        route("/api") {
            authRoutes()
            projectRoutes()
            taskRoutes()
            adminRoutes()
        }
    }
}
