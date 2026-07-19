package com.islami.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "islami_preferences")

class LocalDataSourceImpl(private val context: Context) : LocalDataSource {
    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }.let { flow ->
            var result = defaultValue
            flow.collect { result = it }
            result
        }
    }

    override suspend inline fun <reified T> saveObject(key: String, value: T) {
        val jsonString = json.encodeToString(value)
        saveString(key, jsonString)
    }

    override suspend inline fun <reified T> getObject(key: String): T? {
        val jsonString = getString(key)
        return if (jsonString.isEmpty()) {
            null
        } else {
            try {
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }.let { flow ->
            var result = defaultValue
            flow.collect { result = it }
            result
        }
    }

    override suspend fun saveInt(key: String, value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }

    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }.let { flow ->
            var result = defaultValue
            flow.collect { result = it }
            result
        }
    }

    override fun observeString(key: String, defaultValue: String): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }
    }

    override inline fun <reified T> observeObject(key: String): Flow<T?> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[stringPreferencesKey(key)]
            if (jsonString.isNullOrEmpty()) {
                null
            } else {
                try {
                    json.decodeFromString(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun delete(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(intPreferencesKey(key))
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
