package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.data.tables.*
import com.danioliveira.taskmanager.domain.AppConfig
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

/**
 * Configure the database using the application environment.
 * This method is kept for backward compatibility.
 */
fun Application.configureDatabase() {
    val config = environment.config.config("ktor.database")
    val url = config.property("url").getString()
    val driver = config.property("driver").getString()
    val user = config.property("user").getString()
    val password = config.property("password").getString()

    connectToDatabase(url, driver, user, password)
}

/**
 * Configure the database using the AppConfig.
 */
fun Application.configureDatabase(appConfig: AppConfig) {
    val dbConfig = appConfig.database
    connectToDatabase(dbConfig.url, dbConfig.driver, dbConfig.user, dbConfig.password)
}

/**
 * Connect to the database with the given parameters and create the schema.
 */
private fun connectToDatabase(url: String, driver: String, user: String, password: String) {
    Database.connect(
        url = url,
        driver = driver,
        user = user,
        password = password,
        databaseConfig = DatabaseConfig { defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ }
    )
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

    transaction {
        addLogger(StdOutSqlLogger)
        addLogger(Slf4jSqlDebugLogger)
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
