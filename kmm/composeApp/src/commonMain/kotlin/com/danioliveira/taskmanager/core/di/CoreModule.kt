package com.danioliveira.taskmanager.core.di

import com.danioliveira.taskmanager.core.data.storage.DataStorePreferencesFactory
import com.danioliveira.taskmanager.core.data.storage.DataStoreTokenStorage
import com.danioliveira.taskmanager.core.data.storage.TokenStorage
import com.danioliveira.taskmanager.core.network.KtorClient
import com.danioliveira.taskmanager.core.domain.manager.AuthManager
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    singleOf(::AuthManager)
    single { KtorClient(get(), get()).generateClient() }
    singleOf(::DataStorePreferencesFactory)
    singleOf(::DataStoreTokenStorage) { bind<TokenStorage>() }
}

