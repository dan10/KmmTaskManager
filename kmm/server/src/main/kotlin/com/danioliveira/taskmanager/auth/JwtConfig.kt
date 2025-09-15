package com.danioliveira.taskmanager.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.routes.toUUID
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var realm: String
    private var validityMs: Long = 3600000
    private lateinit var algorithm: Algorithm

    /**
     * Initialize the JWT configuration using the application environment.
     * This method is kept for backward compatibility.
     */
    fun init(environment: ApplicationEnvironment) {
        val config = environment.config.config("ktor.jwt")
        secret = config.property("secret").getString()
        issuer = config.property("issuer").getString()
        audience = config.property("audience").getString()
        realm = config.property("realm").getString()
        validityMs = config.property("validity_ms").getString().toLong()
        algorithm = Algorithm.HMAC256(secret)
    }

    /**
     * Initialize the JWT configuration using the AppConfig.
     */
    fun init(appConfig: AppConfig) {
        secret = appConfig.jwt.secret
        issuer = appConfig.jwt.issuer
        audience = appConfig.jwt.audience
        realm = appConfig.jwt.realm
        validityMs = appConfig.jwt.validityMs
        algorithm = Algorithm.HMAC256(secret)
    }

    fun generateToken(userId: String, email: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
        .sign(algorithm)

    fun configureKtorFeature(config: JWTAuthenticationProvider.Config) {
        config.realm = realm
        config.verifier(
            JWT
                .require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
        )
        config.validate { credential ->
            val userID = credential.payload.getClaim("userId").asString()

            if (userID.isNotEmpty())
                UserPrincipal(userID.toUUID())
            else null
        }
    }
}
