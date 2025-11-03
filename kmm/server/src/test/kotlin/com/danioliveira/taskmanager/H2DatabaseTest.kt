package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.model.UserWithPassword
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.danioliveira.taskmanager.utils.toUuid
import com.danioliveira.taskmanager.utils.randomV7

/**
 * This test demonstrates how to use H2 in-memory database for testing.
 * 
 * Note: These tests are currently disabled due to H2 version incompatibility with r2dbc-h2.
 * The error "NoSuchMethodError: org.h2.command.CommandInterface org.h2.engine.Session.prepareCommand"
 * indicates that the H2 API has changed. All functional tests use TestDatabase instead.
 */
@OptIn(ExperimentalUuidApi::class)
@Ignore("H2 version incompatibility with r2dbc-h2 driver")
class H2DatabaseTest {

    @Before
    fun setup() {
        // Connect to H2 in-memory database
        val h2db = R2dbcDatabase.connect("r2dbc:h2:mem:///test")

        // Create tables
        runBlocking {
            suspendTransaction(h2db) {
                SchemaUtils.create(UsersTable)
            }
        }
    }

    @Test
    fun `test H2 database connection`() {
        // This test verifies that we can connect to the H2 database
        // and create tables
        runBlocking {
            suspendTransaction {
                // If we get here, the connection was successful
                // and the tables were created
                assertNotNull(UsersTable)
            }
        }
    }

    @Test
    fun `test create user in H2 database`() = runBlocking {
        // This test demonstrates how to use the H2 database
        // to test repository operations

        // Initialize the database
        TestDatabase.init()

        // Create a user directly in the database
        val userId = suspendTransaction {
            UsersTable.insert { row ->
                row[UsersTable.email] = "test@example.com"
                row[UsersTable.passwordHash] = "hashed-password"
                row[UsersTable.displayName] = "Test User"
                row[UsersTable.createdAt] = kotlin.time.Clock.System.now()
            }.resultedValues?.first()?.get(UsersTable.id)?.toString() ?: throw IllegalStateException("Failed to create test user")
        }

        // Verify the user was created
        val user = suspendTransaction {
            val row = UsersTable.selectAll().where { UsersTable.id eq userId.toUuid().toJavaUuid() }.singleOrNull()
            assertNotNull(row)

            // Convert to domain model
            UserWithPassword(
                id = row[UsersTable.id].toString(),
                email = row[UsersTable.email],
                displayName = row[UsersTable.displayName],
                googleId = row[UsersTable.googleId],
                createdAt = row[UsersTable.createdAt].toString(),
                passwordHash = row[UsersTable.passwordHash]
            )
        }

        // Verify the user properties
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
        assertEquals("hashed-password", user.passwordHash)
    }
}
