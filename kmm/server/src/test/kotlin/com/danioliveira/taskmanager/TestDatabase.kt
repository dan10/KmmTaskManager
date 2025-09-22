package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.data.tables.ProjectInvitationsTable
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.data.tables.UsersTable
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer

/**
 * Test database helper backed by Testcontainers (PostgreSQL) for Exposed R2DBC.
 */
object TestDatabase {
    private var container: PostgreSQLContainer<Nothing>? = null

    /**
     * Start PostgreSQL Testcontainer and create schema.
     */
    suspend fun init() {
        // Start a lightweight PostgreSQL container
        container = PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
            start()
        }

        val r2dbcDb = R2dbcDatabase.connect {
            connectionFactoryOptions = PostgreSQLR2DBCDatabaseContainer.getOptions(container)
        }

        suspendTransaction(db = r2dbcDb) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(
                UsersTable,
                ProjectsTable,
                TasksTable,
                ProjectInvitationsTable,
                ProjectAssignmentsTable
            )
        }
    }

    /**
     * Truncate all tables to keep tests isolated.
     */
    suspend fun clearDatabase() {
        suspendTransaction {
            ProjectAssignmentsTable.deleteAll()
            ProjectInvitationsTable.deleteAll()
            TasksTable.deleteAll()
            ProjectsTable.deleteAll()
            UsersTable.deleteAll()
        }

        container?.stop()
    }
}