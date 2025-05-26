package com.danioliveira.taskmanager.data.storage

expect class DataStorePreferencesFactory() {

    fun create(name: String): () -> String
}
