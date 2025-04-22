package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.model.UserWithPassword
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * This test demonstrates how to use H2 in-memory database for testing.
 */
class H2DatabaseTest {

    @Before
    fun setup() {
        // Connect to H2 in-memory database
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        // Create tables
        transaction {
            SchemaUtils.create(UsersTable)
        }
    }

    @Test
    fun `test H2 database connection`() {
        // This test verifies that we can connect to the H2 database
        // and create tables
        transaction {
            // If we get here, the connection was successful
            // and the tables were created
            assertNotNull(UsersTable)
        }
    }

    @Test
    fun `test create user in H2 database`() = runBlocking {
        // This test demonstrates how to use the H2 database
        // to test repository operations

        // Initialize the database
        TestDatabase.init()

        // Create a user directly in the database
        val userId = transaction {
            val userEntity = com.danioliveira.taskmanager.data.entity.UserDAOEntity.new {
                email = "test@example.com"
                passwordHash = "hashed-password"
                displayName = "Test User"
                createdAt = java.time.LocalDateTime.now()
            }
            userEntity.id.value.toString()
        }

        // Verify the user was created
        val user = transaction {
            val entity =
                com.danioliveira.taskmanager.data.entity.UserDAOEntity.findById(java.util.UUID.fromString(userId))
            assertNotNull(entity)

            // Convert to domain model
            UserWithPassword(
                id = entity.id.value.toString(),
                email = entity.email,
                displayName = entity.displayName,
                googleId = entity.googleId,
                createdAt = entity.createdAt.toString(),
                passwordHash = entity.passwordHash
            )
        }

        // Verify the user properties
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
        assertEquals("hashed-password", user.passwordHash)
    }
}