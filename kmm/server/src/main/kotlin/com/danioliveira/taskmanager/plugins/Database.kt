package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.data.tables.ProjectInvitationsTable
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.Connection

/**
 * Configure the database using the AppConfig.
 */
fun configureDatabase(appConfig: AppConfig) {
    val dbConfig = appConfig.database
    // Use a default maxPoolSize of 10 if not specified in the config
    val maxPoolSize = 10
    connectToDatabase(dbConfig.url, dbConfig.driver, dbConfig.user, dbConfig.password, maxPoolSize)
}

/**
 * Connect to the database with the given parameters and create the schema.
 * Uses HikariCP for connection pooling.
 */
private fun connectToDatabase(url: String, driver: String, user: String, password: String, maxPoolSize: Int) {
    // Configure HikariCP
    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = url
    hikariConfig.driverClassName = driver
    hikariConfig.username = user
    hikariConfig.password = password
    hikariConfig.maximumPoolSize = maxPoolSize
    hikariConfig.isAutoCommit = false
    hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

    // Connection pool settings
    hikariConfig.minimumIdle = 5
    hikariConfig.idleTimeout = 30000 // 30 seconds
    hikariConfig.maxLifetime = 1800000 // 30 minutes
    hikariConfig.connectionTimeout = 30000 // 30 seconds
    hikariConfig.leakDetectionThreshold = 60000 // 1 minute

    // Add health check properties
    hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    hikariConfig.addDataSourceProperty("useServerPrepStmts", "true")

    // Create HikariDataSource
    val dataSource = HikariDataSource(hikariConfig)

    // Configure Exposed database settings
    val config = DatabaseConfig {
        defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
        maxEntitiesToStoreInCachePerEntity = 100 // Limit entity cache size
        useNestedTransactions = false // Disable nested transactions to reduce connection usage
    }

    // Connect with HikariCP and optimized settings
    Database.connect(
        datasource = dataSource,
        databaseConfig = config
    )

    // Set global transaction manager settings
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

    transaction {
        addLogger(StdOutSqlLogger)
        addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(
            UsersTable,
            ProjectsTable,
            TasksTable,
            ProjectInvitationsTable,
            ProjectAssignmentsTable
        )
    }
}
