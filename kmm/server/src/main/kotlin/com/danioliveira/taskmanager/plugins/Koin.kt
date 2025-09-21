package com.danioliveira.taskmanager.plugins

import com.danioliveira.taskmanager.data.repository.ProjectAssignmentRepositoryImpl
import com.danioliveira.taskmanager.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.data.repository.UserRepositoryImpl
import com.danioliveira.taskmanager.domain.GoogleConfig
import com.danioliveira.taskmanager.domain.repository.UserRepository
import com.danioliveira.taskmanager.domain.service.ProjectService
import com.danioliveira.taskmanager.domain.service.TaskService
import com.danioliveira.taskmanager.domain.service.UserService
import io.ktor.server.application.*
import io.ktor.server.config.property
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import com.danioliveira.taskmanager.domain.repository.ProjectAssignmentRepository as DomainProjectAssignmentRepo
import com.danioliveira.taskmanager.domain.repository.ProjectRepository as DomainProjectRepo
import com.danioliveira.taskmanager.domain.repository.TaskRepository as DomainTaskRepo


fun Application.configureDI() {

    install(Koin) {
        slf4jLogger()
        modules(getAppModule(property<GoogleConfig>("ktor.google")))
    }
}

fun getAppModule(property: GoogleConfig) = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<DomainProjectRepo> { ProjectRepositoryImpl() }
    single<DomainTaskRepo> { TaskRepositoryImpl() }
    single<DomainProjectAssignmentRepo> { ProjectAssignmentRepositoryImpl() }
    single { UserService(get(), property) }
    single { ProjectService(get(), get(), get()) }
    single { TaskService(get(), get(), get()) }
}
