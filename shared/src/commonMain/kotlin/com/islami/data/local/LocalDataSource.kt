package com.islami.data.local

import com.islami.core.serialization.jsonConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Generic interface for local data storage (SharedPreferences, DataStore, Room, etc.)
 */
interface LocalDataSource {
    /**
     * Save a string value
     */
    suspend fun saveString(key: String, value: String)

    /**
     * Get a string value
     */
    suspend fun getString(key: String, defaultValue: String = ""): String

    /**
     * Save a boolean value
     */
    suspend fun saveBoolean(key: String, value: Boolean)

    /**
     * Get a boolean value
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * Save an integer value
     */
    suspend fun saveInt(key: String, value: Int)

    /**
     * Get an integer value
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int

    /**
     * Observe a string value as a flow
     */
    fun observeString(key: String, defaultValue: String = ""): Flow<String>

    /**
     * Delete a value by key
     */
    suspend fun delete(key: String)

    /**
     * Clear all data
     */
    suspend fun clearAll()
}

/**
 * Save a serializable object as JSON
 */
suspend inline fun <reified T> LocalDataSource.saveObject(key: String, value: T) {
    saveString(key, jsonConfig.encodeToString(value))
}

/**
 * Get a serialized object from JSON
 */
suspend inline fun <reified T> LocalDataSource.getObject(key: String): T? {
    val jsonString = getString(key)
    return if (jsonString.isEmpty()) null else {
        try {
            jsonConfig.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Observe a serialized object as a flow
 */
inline fun <reified T> LocalDataSource.observeObject(key: String): Flow<T?> {
    return observeString(key).map { jsonString ->
        if (jsonString.isEmpty()) null else {
            try {
                jsonConfig.decodeFromString<T>(jsonString)
            } catch (e: Exception) {
                null
            }
        }
    }
}
