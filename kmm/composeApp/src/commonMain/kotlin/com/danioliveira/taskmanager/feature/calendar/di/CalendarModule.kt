package com.danioliveira.taskmanager.feature.calendar.di

import com.danioliveira.taskmanager.feature.calendar.ui.CalendarViewModel
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.calendar.GetTasksDueOnUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val calendarModule = module {
    // Use cases
    single { GetTasksDueOnUseCase(get()) }
    
    // ViewModels
    viewModel { CalendarViewModel(get(), get()) }
}



