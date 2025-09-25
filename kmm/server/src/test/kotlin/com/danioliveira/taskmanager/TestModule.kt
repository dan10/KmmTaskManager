package com.danioliveira.taskmanager

import com.danioliveira.taskmanager.data.repository.ProjectAssignmentRepositoryImpl
import com.danioliveira.taskmanager.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.data.repository.UserRepositoryImpl
import com.danioliveira.taskmanager.domain.GoogleConfig
import com.danioliveira.taskmanager.domain.repository.UserRepository
import com.danioliveira.taskmanager.domain.service.ProjectService
import com.danioliveira.taskmanager.domain.service.TaskService
import com.danioliveira.taskmanager.domain.service.UserService
import org.koin.core.module.Module
import org.koin.dsl.module
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository as DomainProjectAssignmentRepo
import com.danioliveira.taskmanager.domain.repository.ProjectRepository as DomainProjectRepo
import com.danioliveira.taskmanager.domain.repository.TaskRepository as DomainTaskRepo

/**
 * Creates a Koin module for testing that uses real repositories with H2 database.
 * This replaces the fake repositories used in tests with real implementations.
 */
fun getTestModule(): Module = module {

    // Use real repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<DomainProjectRepo> { ProjectRepositoryImpl() }
    single<DomainTaskRepo> { TaskRepositoryImpl() }
    single<DomainProjectAssignmentRepo> { ProjectAssignmentRepositoryImpl() }

    // Services
    single { UserService(get(), GoogleConfig("test")) }
    single { ProjectService(get(), get(), get()) }
    single { TaskService(get(), get(), get()) }
}


