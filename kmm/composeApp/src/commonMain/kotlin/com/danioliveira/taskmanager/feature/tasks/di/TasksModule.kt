package com.danioliveira.taskmanager.feature.tasks.di

import com.danioliveira.taskmanager.feature.tasks.data.network.TaskApiService
import com.danioliveira.taskmanager.feature.tasks.data.repository.TaskRepositoryImpl
import com.danioliveira.taskmanager.feature.tasks.domain.repository.TaskRepository
import com.danioliveira.taskmanager.feature.tasks.domain.search.TaskSearchHandler
import com.danioliveira.taskmanager.feature.tasks.domain.search.TaskSearchHandlerImpl
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.CreateEditTaskUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.DeleteTaskUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTaskDetailsUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTaskProgressUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTasksUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.UpdateTaskStatusUseCase
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.details.TasksDetailsViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.edit.EditTaskViewModel
import com.danioliveira.taskmanager.feature.tasks.ui.list.TasksViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tasksModule = module {
    singleOf(::TaskApiService)
    singleOf(::TaskRepositoryImpl) { bind<TaskRepository>() }
    singleOf(::TaskSearchHandlerImpl) { bind<TaskSearchHandler>() }

    factoryOf(::GetTasksUseCase)
    factoryOf(::GetTaskProgressUseCase)
    factoryOf(::CreateEditTaskUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::GetTaskDetailsUseCase)
    factoryOf(::UpdateTaskStatusUseCase)

    viewModelOf(::TasksViewModel)
    viewModelOf(::TasksDetailsViewModel)
    viewModelOf(::TaskCreateViewModel)
    
    // EditTaskViewModel with taskId parameter
    factoryOf(::EditTaskViewModel)
}
