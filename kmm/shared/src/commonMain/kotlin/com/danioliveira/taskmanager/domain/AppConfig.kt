package com.danioliveira.taskmanager.domain

import kotlinx.serialization.Serializable

/**
 * Configuration model for the application.
 * This class centralizes all configuration values used across the application.
 */
@Serializable
data class AppConfig(
    val server: ServerConfig,
    val jwt: JwtConfig,
    val database: DatabaseConfig,
    val google: GoogleConfig
)

/**
 * Server configuration.
 */
@Serializable
data class ServerConfig(
    val port: Int,
    val host: String
)

/**
 * JWT configuration.
 */
@Serializable
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val validityMs: Long
)

/**
 * Database configuration.
 */
@Serializable
data class DatabaseConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String
)

/**
 * Google configuration.
 */
@Serializable
data class GoogleConfig(
    val clientId: String
)