package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.data.tables.ProjectAssignmentsTable
import com.danioliveira.taskmanager.data.tables.ProjectInvitationsTable
import com.danioliveira.taskmanager.data.tables.ProjectsTable
import com.danioliveira.taskmanager.data.tables.TasksTable
import com.danioliveira.taskmanager.data.tables.UsersTable
import com.danioliveira.taskmanager.domain.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.sql.Connection
import org.jetbrains.exposed.v1.jdbc.Database as JdbcDatabase
import org.jetbrains.exposed.v1.jdbc.SchemaUtils as JdbcSchemaUtils
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils as R2dbcSchemaUtils

/**
 * Configure the database using the AppConfig.
 */
fun configureDatabase(appConfig: AppConfig) {
    val db = appConfig.database
    val maxPoolSize = 10
    if (db.url.startsWith("r2dbc:")) {
        connectR2dbc(db.url)
    } else {
        connectJdbc(db.url, db.driver, db.user, db.password, maxPoolSize)
    }
}

private fun connectJdbc(url: String, driver: String, user: String, password: String, maxPoolSize: Int) {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        this.password = password
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        minimumIdle = 5
        idleTimeout = 30000
        maxLifetime = 1800000
        connectionTimeout = 30000
        leakDetectionThreshold = 60000
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        addDataSourceProperty("useServerPrepStmts", "true")
    }
    val dataSource = HikariDataSource(hikariConfig)

    val config = DatabaseConfig {
        defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
        maxEntitiesToStoreInCachePerEntity = 100
        useNestedTransactions = false
    }


    JdbcDatabase.connect(
        datasource = dataSource,
        databaseConfig = config
    )

    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ

    transaction {
        addLogger(StdOutSqlLogger)
        addLogger(Slf4jSqlDebugLogger)
        JdbcSchemaUtils.create(
            UsersTable,
            ProjectsTable,
            TasksTable,
            ProjectInvitationsTable,
            ProjectAssignmentsTable
        )
    }
}

private fun connectR2dbc(url: String) {
    val config = DatabaseConfig {
        maxEntitiesToStoreInCachePerEntity = 100
        useNestedTransactions = false
    }

    R2dbcDatabaseConfig {

    }

    val connectionFactory = ConnectionFactories.get(url)
    R2dbcDatabase.connect(
        connectionFactory = connectionFactory,
        databaseConfig = config
    )

    runBlocking {
        suspendTransaction {
            // Optionally attach loggers; Exposed R2DBC supports StdOut logger in transactions
            R2dbcSchemaUtils.create(
                UsersTable,
                ProjectsTable,
                TasksTable,
                ProjectInvitationsTable,
                ProjectAssignmentsTable
            )
        }
    }
}
