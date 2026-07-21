package com.islami.data.local

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalDataSourceImpl : LocalDataSource {
    private val json = Json { ignoreUnknownKeys = true }
    private val _dataChanged = MutableStateFlow(0)

    override suspend fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
        _dataChanged.value++
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        return localStorage.getItem(key) ?: defaultValue
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
        saveString(key, value.toString())
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return localStorage.getItem(key)?.toBoolean() ?: defaultValue
    }

    override suspend fun saveInt(key: String, value: Int) {
        saveString(key, value.toString())
    }

    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return localStorage.getItem(key)?.toIntOrNull() ?: defaultValue
    }

    override fun observeString(key: String, defaultValue: String): Flow<String> {
        // Simple implementation: emit current value and updates
        return MutableStateFlow(localStorage.getItem(key) ?: defaultValue).asStateFlow()
    }

    override inline fun <reified T> observeObject(key: String): Flow<T?> {
        return MutableStateFlow(getObject<T>(key)).asStateFlow()
    }

    override suspend fun delete(key: String) {
        localStorage.removeItem(key)
        _dataChanged.value++
    }

    override suspend fun clearAll() {
        localStorage.clear()
        _dataChanged.value++
    }
}
