package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.config.AppConfigImpl
import com.danioliveira.taskmanager.data.repository.ProjectAssignmentRepositoryImpl
import com.danioliveira.taskmanager.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.data.repository.UserRepositoryImpl
import com.danioliveira.taskmanager.domain.AppConfig
import com.danioliveira.taskmanager.domain.repository.UserRepository
import com.danioliveira.taskmanager.domain.service.ProjectService
import com.danioliveira.taskmanager.domain.service.TaskService
import com.danioliveira.taskmanager.domain.service.UserService
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.core.module.Module
import org.koin.dsl.module
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository as DomainProjectAssignmentRepo
import com.danioliveira.taskmanager.domain.repository.ProjectRepository as DomainProjectRepo
import com.danioliveira.taskmanager.domain.repository.TaskRepository as DomainTaskRepo

/**
 * Creates a Koin module for testing that uses real repositories with H2 database.
 * This replaces the fake repositories used in tests with real implementations.
 */
fun getTestModule(config: ApplicationConfig): Module = module {
    // Create a test environment with the provided config
    val testEnvironment = object : ApplicationEnvironment {
        override val config = config
        override val log = org.slf4j.LoggerFactory.getLogger("TestEnvironment")
        override val classLoader = Thread.currentThread().contextClassLoader
        override val monitor = io.ktor.events.Events()
    }

    // Use the test configuration with AppConfigImpl
    single<AppConfig> { AppConfigImpl(testEnvironment).config }

    // Use real repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<DomainProjectRepo> { ProjectRepositoryImpl() }
    single<DomainTaskRepo> { TaskRepositoryImpl() }
    single<DomainProjectAssignmentRepo> { ProjectAssignmentRepositoryImpl() }

    // Services
    single { UserService(get(), get()) }
    single { ProjectService(get(), get()) }
    single { TaskService(get(), get()) }
}
