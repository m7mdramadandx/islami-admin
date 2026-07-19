package com.islami.data.local

import kotlinx.coroutines.flow.Flow

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
     * Save a serializable object as JSON
     */
    suspend inline fun <reified T> saveObject(key: String, value: T)

    /**
     * Get a serialized object from JSON
     */
    suspend inline fun <reified T> getObject(key: String): T?

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
     * Observe a serialized object as a flow
     */
    inline fun <reified T> observeObject(key: String): Flow<T?>

    /**
     * Delete a value by key
     */
    suspend fun delete(key: String)

    /**
     * Clear all data
     */
    suspend fun clearAll()
}
