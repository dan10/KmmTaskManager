package com.danioliveira.taskmanager.feature.auth.di

import com.danioliveira.taskmanager.feature.auth.data.network.AuthApiService
import com.danioliveira.taskmanager.feature.auth.data.repository.AuthRepositoryImpl
import com.danioliveira.taskmanager.feature.auth.domain.repository.AuthRepository
import com.danioliveira.taskmanager.feature.auth.domain.usecase.login.LoginUseCase
import com.danioliveira.taskmanager.feature.auth.domain.usecase.register.RegisterUseCase
import com.danioliveira.taskmanager.feature.auth.ui.login.LoginViewModel
import com.danioliveira.taskmanager.feature.auth.ui.register.RegisterViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthApiService)
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }

    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}

