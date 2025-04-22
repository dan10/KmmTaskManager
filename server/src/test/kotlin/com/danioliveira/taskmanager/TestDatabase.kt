package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.data.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

/**
 * Configures an H2 in-memory database for testing.
 * This allows tests to run without requiring a PostgreSQL instance.
 */
object TestDatabase {
    /**
     * Initializes the H2 in-memory database for testing.
     * Creates all necessary tables and sets up the database connection.
     */
    fun init() {
        // Connect to H2 in-memory database
        Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "sa",
            password = "",
            databaseConfig = DatabaseConfig { defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ }
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

        // Create tables
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(
                UsersTable,
                ProjectsTable,
                TasksTable,
                FileUploadsTable,
                ProjectInvitationsTable,
                ProjectAssignmentsTable
            )
        }
    }

    /**
     * Clears all data from the database tables.
     * This is useful for ensuring tests start with a clean state.
     */
    fun clearDatabase() {
        transaction {
            ProjectAssignmentsTable.deleteAll()
            ProjectInvitationsTable.deleteAll()
            FileUploadsTable.deleteAll()
            TasksTable.deleteAll()
            ProjectsTable.deleteAll()
            UsersTable.deleteAll()
        }
    }
}