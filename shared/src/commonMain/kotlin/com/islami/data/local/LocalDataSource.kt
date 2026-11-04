package com.islami.data.local

/** Small key-value store on this device (DataStore on Android, localStorage on the web). */
interface LocalDataSource {
    suspend fun saveString(key: String, value: String)

    suspend fun getString(key: String, defaultValue: String = ""): String

    suspend fun delete(key: String)
}
