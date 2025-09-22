package com.danioliveira.taskmanager.data.repository

import com.danioliveira.taskmanager.TestDatabase
import com.danioliveira.taskmanager.data.dbQuery
import com.danioliveira.taskmanager.domain.repository.UserRepository
import com.danioliveira.taskmanager.routes.toUUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryImplTest {
    private lateinit var repository: UserRepository

    @Before
    fun setUp() = runBlocking {
        TestDatabase.init()
        repository = UserRepositoryImpl()
    }

    @After
    fun tearDown() = runBlocking {
        TestDatabase.clearDatabase()
    }

    @Test
    fun `test create and find user by email`() = runBlocking {
        // Create a user
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        val user = dbQuery {
            repository.create(email, passwordHash, displayName, null)
        }

        // Verify the user was created correctly
        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(passwordHash, user.passwordHash)

        // Find the user by email
        val foundUser = dbQuery {
            repository.findByEmail(email)
        }

        // Verify the user was found
        assertNotNull(foundUser)
        assertEquals(user.id, foundUser.id)
        assertEquals(email, foundUser.email)
    }

    @Test
    fun `test find user by id`() = runTest {
        // Create a user
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        val user = dbQuery {
            repository.create(email, passwordHash, displayName, null)
        }

        // Find the user by ID
        val foundUser = dbQuery {
            repository.findById(user.id.toUUID())
        }

        // Verify the user was found
        assertNotNull(foundUser)
        assertEquals(user.id, foundUser.id)
        assertEquals(email, foundUser.email)
        assertEquals(displayName, foundUser.displayName)
    }

    @Test
    fun `test find non-existent user`() = runBlocking {
        // Try to find a user that doesn't exist by email
        val user = dbQuery {
            repository.findByEmail("nonexistent@example.com")
        }

        // Verify the user was not found
        assertNull(user)

        // Try to find a user that doesn't exist by ID
        val userById = dbQuery {
            repository.findById(UUID.randomUUID())
        }

        // Verify the user was not found
        assertNull(userById)
    }

    @Test
    fun `test create user with google id`() = runBlocking {
        // Create a user with Google ID
        val email = "google@example.com"
        val displayName = "Google User"
        val googleId = "google-123456"

        val user = dbQuery {
            repository.create(email, null, displayName, googleId)
        }

        // Verify the user was created correctly
        assertNotNull(user)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(googleId, user.googleId)
        assertNull(user.passwordHash)

        // Find the user by email
        val foundUser = dbQuery {
            repository.findByEmail(email)
        }

        // Verify the user was found with the correct Google ID
        assertNotNull(foundUser)
        assertEquals(googleId, foundUser.googleId)
    }

    @Test
    fun `test to safe user`() = runBlocking {
        // Create a user
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed-password"

        val user = dbQuery {
            repository.create(email, passwordHash, displayName, null)
        }

        // Convert to safe user
        val safeUser = repository.toSafeUser(user)

        // Verify the safe user doesn't have a password hash
        assertEquals(user.id, safeUser.id)
        assertEquals(email, safeUser.email)
        assertEquals(displayName, safeUser.displayName)
    }
}
