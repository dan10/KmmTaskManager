package com.danioliveira.taskmanager.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

class DataStoreTokenStorage(
    private val factory: DataStorePreferencesFactory
) : TokenStorage {

    // Define the key for the token
    private val tokenKey = stringPreferencesKey("auth_token")

    private val dataStore = PreferenceDataStoreFactory.createWithPath(
        produceFile = { factory.create("auth_token.preferences_pb")().toPath() }
    )

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    override suspend fun getToken(): String? {
        return dataStore.data
            .map { preferences -> preferences[tokenKey] }
            .firstOrNull()
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}