package com.islami.data.local

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalDataSourceImpl : LocalDataSource {
    private val _dataChanged = MutableStateFlow(0)

    override suspend fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
        _dataChanged.value++
    }

    override suspend fun getString(key: String, defaultValue: String): String {
        return localStorage.getItem(key) ?: defaultValue
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


    override suspend fun delete(key: String) {
        localStorage.removeItem(key)
        _dataChanged.value++
    }

    override suspend fun clearAll() {
        localStorage.clear()
        _dataChanged.value++
    }
}
