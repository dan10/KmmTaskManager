package com.danioliveira.taskmanager.di

import com.danioliveira.taskmanager.core.di.coreModule
import com.danioliveira.taskmanager.feature.auth.di.authModule
import com.danioliveira.taskmanager.feature.projects.di.projectsModule
import com.danioliveira.taskmanager.feature.tasks.di.tasksModule
import org.koin.dsl.module

val appModule = module {
    includes(coreModule, authModule, projectsModule, tasksModule)
}
