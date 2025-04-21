package com.danioliveira.taskmanager.config

import com.danioliveira.taskmanager.SERVER_PORT
import com.danioliveira.taskmanager.domain.*
import com.danioliveira.taskmanager.domain.ServerConfig
import io.ktor.server.application.*

/**
 * Implementation of AppConfig that reads values from the application environment.
 */
class AppConfigImpl(environment: ApplicationEnvironment) {

    val config: AppConfig

    init {
        // Read JWT configuration
        val jwtConfig = environment.config.config("ktor.jwt")
        val jwt = JwtConfig(
            secret = jwtConfig.property("secret").getString(),
            issuer = jwtConfig.property("issuer").getString(),
            audience = jwtConfig.property("audience").getString(),
            realm = jwtConfig.property("realm").getString(),
            validityMs = jwtConfig.property("validity_ms").getString().toLong()
        )

        // Read database configuration
        val dbConfig = environment.config.config("ktor.database")
        val database = DatabaseConfig(
            url = dbConfig.property("url").getString(),
            driver = dbConfig.property("driver").getString(),
            user = dbConfig.property("user").getString(),
            password = dbConfig.property("password").getString()
        )

        // Read Google configuration
        val google = GoogleConfig(
            clientId = environment.config.property("ktor.google.client_id").getString()
        )

        // Create server configuration
        val server = ServerConfig(
            port = SERVER_PORT,
            host = "0.0.0.0"
        )

        // Create the final AppConfig
        config = AppConfig(
            server = server,
            jwt = jwt,
            database = database,
            google = google
        )
    }
}