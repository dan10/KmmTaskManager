package com.danioliveira.taskmanager.di

import org.koin.core.context.startKoin

/**
 * Initializes Koin dependency injection.
 */
object KoinInitializer {

    /**
     * Initializes Koin with the application modules.
     */
    fun initialize() {
        startKoin {
            modules(appModule)
        }
    }
}