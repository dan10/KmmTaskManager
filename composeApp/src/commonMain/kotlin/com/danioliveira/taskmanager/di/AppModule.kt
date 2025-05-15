package com.danioliveira.taskmanager.di

import com.danioliveira.taskmanager.data.network.AuthApiService
import com.danioliveira.taskmanager.data.network.KtorClient
import com.danioliveira.taskmanager.data.network.ProjectApiService
import com.danioliveira.taskmanager.data.network.TaskApiService
import com.danioliveira.taskmanager.data.repository.AuthRepositoryImpl
import com.danioliveira.taskmanager.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.data.storage.DataStorePreferencesFactory
import com.danioliveira.taskmanager.data.storage.DataStoreTokenStorage
import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.domain.repository.AuthRepository
import com.danioliveira.taskmanager.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import com.danioliveira.taskmanager.domain.usecase.login.LoginUseCase
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectDetailsUseCase
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectTasksUseCase
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectsUseCase
import com.danioliveira.taskmanager.domain.usecase.register.RegisterUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.CreateEditTaskUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTaskDetailsUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTaskProgressUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTasksUseCase
import com.danioliveira.taskmanager.ui.login.LoginViewModel
import com.danioliveira.taskmanager.ui.project.ProjectDetailsViewModel
import com.danioliveira.taskmanager.ui.projects.ProjectsViewModel
import com.danioliveira.taskmanager.ui.register.RegisterViewModel
import com.danioliveira.taskmanager.ui.task.create.TaskCreateEditViewModel
import com.danioliveira.taskmanager.ui.task.details.TasksDetailsViewModel
import com.danioliveira.taskmanager.ui.tasks.TasksViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for the application.
 */
val appModule = module {

    single { KtorClient(get()).generateClient() }
    // Network
    singleOf(::AuthApiService)
    singleOf(::TaskApiService)
    singleOf(::ProjectApiService)

    // Storage
    singleOf(::DataStorePreferencesFactory)
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }

    // Repositories
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::TaskRepositoryImpl) { bind<TaskRepository>() }
    singleOf(::ProjectRepositoryImpl) { bind<ProjectRepository>() }

    // Managers
    singleOf(::AuthManager)

    // Use Cases
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::GetTasksUseCase)
    factoryOf(::GetTaskProgressUseCase)
    factoryOf(::GetProjectsUseCase)
    factoryOf(::GetProjectDetailsUseCase)
    factoryOf(::GetProjectTasksUseCase)
    factoryOf(::CreateEditTaskUseCase)
    factoryOf(::GetTaskDetailsUseCase)

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectDetailsViewModel)
    viewModelOf(::TaskCreateEditViewModel)
    viewModelOf(::TasksDetailsViewModel)
}
