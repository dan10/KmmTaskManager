package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.data.tables.ProjectInvitationsTable
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.DatabaseConfig
import io.ktor.server.application.Application
import io.ktor.server.config.property
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils as R2dbcSchemaUtils

fun Application.configureDatabase() {
    val databaseConfig = property<DatabaseConfig>("ktor.database")

    val db = R2dbcDatabase.connect {
        connectionFactoryOptions {
            option(ConnectionFactoryOptions.DRIVER, databaseConfig.driverClassName)
            option(ConnectionFactoryOptions.HOST, databaseConfig.host)
            option(ConnectionFactoryOptions.PORT, databaseConfig.port)
            option(ConnectionFactoryOptions.DATABASE, databaseConfig.databaseName)
            option(ConnectionFactoryOptions.USER, databaseConfig.user)
            option(ConnectionFactoryOptions.PASSWORD, databaseConfig.password)
        }
    }

    runBlocking {
        suspendTransaction(db = db) {
            addLogger(StdOutSqlLogger)
            addLogger(Slf4jSqlDebugLogger)
            val allTables = arrayOf(
                UsersTable,
                ProjectsTable,
                TasksTable,
                ProjectInvitationsTable,
                ProjectAssignmentsTable
            )

          //  R2dbcSchemaUtils.drop(tables = allTables.reversedArray())
            R2dbcSchemaUtils.create(tables = allTables)
        }
    }
}






