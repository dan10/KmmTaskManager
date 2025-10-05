package com.danioliveira.taskmanager.core.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danioliveira.taskmanager.domain.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

class DataStoreTokenStorage(
    private val factory: DataStorePreferencesFactory
) : TokenStorage {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val userKey = stringPreferencesKey("auth_user")

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

    override suspend fun saveUser(user: User) {
        dataStore.edit { preferences ->
            preferences[userKey] = Json.encodeToString(user)
        }
    }

    override suspend fun getUser(): User? {
        return runCatching {
            dataStore.data
                .map { preferences ->
                    preferences[userKey]?.let { Json.decodeFromString<User>(it) }
                }
                .firstOrNull()
        }.getOrNull()
    }

    override suspend fun clearUser() {
        dataStore.edit { preferences ->
            preferences.remove(userKey)
        }
    }
}