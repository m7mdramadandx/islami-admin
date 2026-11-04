package com.islami.data.local

import kotlinx.browser.localStorage

class LocalDataSourceImpl : LocalDataSource {
    override suspend fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override suspend fun getString(key: String, defaultValue: String): String =
        localStorage.getItem(key) ?: defaultValue

    override suspend fun delete(key: String) {
        localStorage.removeItem(key)
    }
}
