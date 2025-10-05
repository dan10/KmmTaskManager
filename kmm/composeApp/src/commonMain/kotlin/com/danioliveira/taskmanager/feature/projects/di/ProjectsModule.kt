package com.danioliveira.taskmanager.feature.projects.di

import com.danioliveira.taskmanager.feature.projects.data.network.ProjectApiService
import com.danioliveira.taskmanager.feature.projects.data.repository.ProjectRepositoryImpl
import com.danioliveira.taskmanager.feature.projects.domain.repository.ProjectRepository
import com.danioliveira.taskmanager.feature.projects.domain.usecase.CreateEditProjectUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectDetailsUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectTasksUseCase
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectsUseCase
import com.danioliveira.taskmanager.feature.projects.ui.create.create.CreateEditProjectViewModel
import com.danioliveira.taskmanager.feature.projects.ui.details.ProjectDetailsViewModel
import com.danioliveira.taskmanager.feature.projects.ui.list.ProjectsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val projectsModule = module {
    singleOf(::ProjectApiService)
    singleOf(::ProjectRepositoryImpl) { bind<ProjectRepository>() }

    factoryOf(::GetProjectsUseCase)
    factoryOf(::GetProjectDetailsUseCase)
    factoryOf(::GetProjectTasksUseCase)
    factoryOf(::CreateEditProjectUseCase)

    viewModelOf(::ProjectsViewModel)
    viewModelOf(::ProjectDetailsViewModel)
    viewModelOf(::CreateEditProjectViewModel)
}

