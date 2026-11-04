package com.islami.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "islami_preferences")

class LocalDataSourceImpl(private val context: Context) : LocalDataSource {
    private val dataStore = context.dataStore

    override suspend fun saveString(key: String, value: String) {
        dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    // DataStore's flow never completes, so take the current value rather than collecting it.
    override suspend fun getString(key: String, defaultValue: String): String =
        dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue

    override suspend fun delete(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }
}
