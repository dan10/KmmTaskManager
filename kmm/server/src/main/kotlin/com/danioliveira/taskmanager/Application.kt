package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.plugins.configureDI
import com.danioliveira.taskmanager.plugins.configureDatabase
import com.danioliveira.taskmanager.plugins.configureMetrics
import com.danioliveira.taskmanager.plugins.configureRequestValidation
import com.danioliveira.taskmanager.plugins.configureSecurity
import com.danioliveira.taskmanager.plugins.configureSerialization
import com.danioliveira.taskmanager.plugins.configureStatusPages
import com.danioliveira.taskmanager.routes.adminRoutes
import com.danioliveira.taskmanager.routes.authRoutes
import com.danioliveira.taskmanager.routes.projectMemberRoutes
import com.danioliveira.taskmanager.routes.projectRoutes
import com.danioliveira.taskmanager.routes.projectTaskRoutes
import com.danioliveira.taskmanager.routes.taskRoutes
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    // Configure plugins
    configureDI()

    // Get AppConfig from Koin
    val appConfig by inject<AppConfig>()

    configureDatabase()
    JwtConfig.init(appConfig)
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureRequestValidation()
    configureMetrics()

    routing {
        authRoutes()
        projectRoutes()
        projectTaskRoutes()
        projectMemberRoutes()
        taskRoutes()
        adminRoutes()
    }
}
