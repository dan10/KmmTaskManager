package com.danioliveira.taskmanager.core.data.storage

expect class DataStorePreferencesFactory() {
    fun create(name: String): () -> String
}
