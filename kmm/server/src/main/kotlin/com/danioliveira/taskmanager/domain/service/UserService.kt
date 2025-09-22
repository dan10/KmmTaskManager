package com.danioliveira.taskmanager.domain.service

import com.danioliveira.taskmanager.api.request.GoogleLoginRequest
import com.danioliveira.taskmanager.api.request.LoginRequest
import com.danioliveira.taskmanager.api.request.RegisterRequest
import com.danioliveira.taskmanager.api.response.AuthResponse
import com.danioliveira.taskmanager.auth.GoogleTokenVerifier
import com.danioliveira.taskmanager.auth.JwtConfig
import com.danioliveira.taskmanager.auth.PasswordHasher
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.GoogleConfig
import com.danioliveira.taskmanager.domain.User
import com.danioliveira.taskmanager.domain.exceptions.NotFoundException
import com.danioliveira.taskmanager.domain.exceptions.UnauthorizedException
import com.danioliveira.taskmanager.domain.exceptions.ValidationException
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import com.danioliveira.taskmanager.domain.repository.UserRepository
import java.util.UUID

class UserService(
    private val repository: UserRepository,
    private val googleConfig: GoogleConfig
) {
    suspend fun findByEmail(email: String): UserWithPassword? = dbQuery {
        repository.findByEmail(email)
    }

    suspend fun findById(id: UUID): UserWithPassword = dbQuery {
        repository.findById(id) ?: throw NotFoundException("User", id.toString())
    }

    suspend fun create(email: String, passwordHash: String?, displayName: String, googleId: String?): UserWithPassword =
        dbQuery {
            repository.create(email, passwordHash, displayName, googleId)
        }

    fun toSafeUser(user: UserWithPassword): User = repository.toSafeUser(user)

    /**
     * Registers a new user with the given information.
     *
     * @param request The registration request containing email, password, and display name
     * @return An AuthResponse containing the JWT token and the user object
     * @throws ValidationException if the email is already registered
     */

    suspend fun register(request: RegisterRequest): AuthResponse {
        val existingUser = findByEmail(request.email)
        if (existingUser != null) {
            throw ValidationException(
                message = "Registration failed",
                errors = mapOf("email" to "Email is already registered")
            )
        }

        val hash = PasswordHasher.hash(request.password)
        val userWithPassword = create(request.email, hash, request.displayName, null)
        val token = JwtConfig.generateToken(userWithPassword.id, userWithPassword.email)
        val safeUser = toSafeUser(userWithPassword)

       return AuthResponse(token = token, user = safeUser)
    }

    /**
     * Authenticates a user with the given credentials.
     *
     * @param request The login request containing email and password
     * @return An AuthResponse containing the JWT token and the user object
     * @throws UnauthorizedException if the credentials are invalid
     */
    suspend fun login(request: LoginRequest): AuthResponse {
        val userWithPassword = findByEmail(request.email)
        if (userWithPassword == null ||
            userWithPassword.passwordHash == null ||
            !PasswordHasher.verify(request.password, userWithPassword.passwordHash)
        ) {
            throw UnauthorizedException("Invalid email or password")
        }

        val token = JwtConfig.generateToken(userWithPassword.id, userWithPassword.email)
        val safeUser = toSafeUser(userWithPassword)

        return AuthResponse(token = token, user = safeUser)
    }

    /**
     * Authenticates a user with Google.
     *
     * @param request The Google login request containing the ID token
     * @return An AuthResponse containing the JWT token and the user object
     * @throws UnauthorizedException if the Google ID token is invalid
     */
    suspend fun googleLogin(request: GoogleLoginRequest): AuthResponse {
        val clientId = googleConfig.clientId
        val payload = GoogleTokenVerifier.verify(request.idToken, clientId)
        if (payload == null) {
            throw UnauthorizedException("Invalid Google ID token")
        }

        val email = payload.email
        val displayName = payload["name"] as? String ?: email
        val googleId = payload.subject

        var userWithPassword = findByEmail(email)
        if (userWithPassword == null) {
            userWithPassword = create(email, null, displayName, googleId)
        }

        val token = JwtConfig.generateToken(userWithPassword.id, userWithPassword.email)
        val safeUser = toSafeUser(userWithPassword)

        return AuthResponse(token = token, user = safeUser)
    }
}
