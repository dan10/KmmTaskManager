package com.danioliveira.taskmanager

import android.app.Application
import com.danioliveira.taskmanager.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class TaskItApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Koin
        startKoin {
            androidContext(this@TaskItApplication)
            modules(appModule)
        }
    }
}