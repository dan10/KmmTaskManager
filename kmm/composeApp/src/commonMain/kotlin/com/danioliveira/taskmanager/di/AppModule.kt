package com.danioliveira.taskmanager.di

import com.danioliveira.taskmanager.data.network.AuthApiService
import com.danioliveira.taskmanager.data.network.KtorClient
import com.danioliveira.taskmanager.data.network.ProjectApiService
import com.danioliveira.taskmanager.core.data.storage.DataStorePreferencesFactory
import com.danioliveira.taskmanager.data.storage.DataStoreTokenStorage
import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.feature.auth.data.repository.AuthRepositoryImpl
import com.danioliveira.taskmanager.feature.auth.domain.repository.AuthRepository
import com.danioliveira.taskmanager.feature.auth.domain.usecase.login.LoginUseCase
import com.danioliveira.taskmanager.feature.auth.domain.usecase.register.RegisterUseCase
import com.danioliveira.taskmanager.feature.auth.ui.login.LoginViewModel
import com.danioliveira.taskmanager.feature.auth.ui.register.RegisterViewModel
import com.danioliveira.taskmanager.feature.projects.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.feature.projects.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.feature.projects.domain.usecase.CreateEditProjectUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectDetailsUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectTasksUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectsUseCase
import com.danioliveira.taskmanager.feature.projects.ui.create.create.CreateEditProjectViewModel
import com.danioliveira.taskmanager.feature.projects.ui.details.ProjectDetailsViewModel
import com.danioliveira.taskmanager.feature.projects.ui.list.ProjectsViewModel
import com.danioliveira.taskmanager.feature.tasks.data.network.TaskApiService
import com.danioliveira.taskmanager.feature.tasks.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.CreateEditTaskUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.DeleteTaskUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTaskDetailsUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTaskProgressUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTasksUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.UpdateTaskStatusUseCase
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateEditViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.details.TasksDetailsViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.list.TasksViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for the application.
 */
val appModule = module {

    singleOf(::AuthManager)

    single { KtorClient(get(), get()).generateClient() }
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


    // Use Cases
    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::GetTasksUseCase)
    factoryOf(::GetTaskProgressUseCase)
    factoryOf(::GetProjectsUseCase)
    factoryOf(::GetProjectDetailsUseCase)
    factoryOf(::GetProjectTasksUseCase)
    factoryOf(::CreateEditTaskUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::GetTaskDetailsUseCase)
    factoryOf(::CreateEditProjectUseCase)
    factoryOf(::UpdateTaskStatusUseCase)

    // ViewModels
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectDetailsViewModel)
    viewModelOf(::CreateEditProjectViewModel)
    viewModelOf(::TaskCreateEditViewModel)
    viewModelOf(::TasksDetailsViewModel)
}
