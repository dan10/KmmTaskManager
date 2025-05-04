package com.danioliveira.taskmanager.data.storage

import android.content.Context
import org.koin.java.KoinJavaComponent.inject

actual class DataStorePreferencesFactory actual constructor() {

    private val context by inject<Context>(Context::class.java)

    actual fun create(name: String): () -> String {
        return {
            context.filesDir.resolve(name).absolutePath
        }
    }
}