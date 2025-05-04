package com.danioliveira.taskmanager.di

import com.danioliveira.taskmanager.data.network.AuthApiService
import com.danioliveira.taskmanager.data.network.KtorClient
import com.danioliveira.taskmanager.data.repository.AuthRepositoryImpl
import com.danioliveira.taskmanager.data.storage.DataStorePreferencesFactory
import com.danioliveira.taskmanager.data.storage.DataStoreTokenStorage
import com.danioliveira.taskmanager.data.storage.TokenStorage
import com.danioliveira.taskmanager.domain.repository.AuthRepository
import com.danioliveira.taskmanager.domain.usecase.login.LoginUseCase
import com.danioliveira.taskmanager.ui.login.LoginViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for the application.
 */
val appModule = module {

    single { KtorClient(get()).generateClient() }
    // Network
    singleOf(::AuthApiService)

    // Storage
    singleOf(::DataStorePreferencesFactory)
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }

    // Repositories
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class

    // Use Cases
    factoryOf(::LoginUseCase)

    // ViewModels
    factoryOf(::LoginViewModel)
}
