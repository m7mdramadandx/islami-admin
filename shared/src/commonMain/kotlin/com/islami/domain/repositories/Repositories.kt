package com.islami.domain.repositories

import com.islami.core.error.Result
import com.islami.domain.entities.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUser(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(email: String, password: String, displayName: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun getCurrentUser(): User?
}

interface HomeRepository {
    suspend fun getHomeStats(): Result<HomeStats>
    suspend fun getAppSettings(): Result<AppSettings>
}

interface FeedbackRepository {
    suspend fun getFeedbackMessages(limit: Int = 100): Result<List<FeedbackMessage>>
    suspend fun getFeedbackMessage(id: String): Result<FeedbackMessage>
    suspend fun deleteFeedbackMessage(id: String): Result<Unit>
    suspend fun respondToFeedback(id: String, response: String): Result<Unit>
}

interface QuranRepository {
    suspend fun getAllSurahs(): Result<List<Surah>>
    suspend fun getSurah(number: Int): Result<Surah>
    suspend fun getSurahAyahs(surahNumber: Int): Result<List<Ayah>>
    suspend fun searchAyahs(query: String): Result<List<Ayah>>
    fun observeSurahs(): Flow<Result<List<Surah>>>
}

interface HadithRepository {
    suspend fun getAllHadith(): Result<List<Hadith>>
    suspend fun getHadithByBook(book: String): Result<List<Hadith>>
    suspend fun searchHadith(query: String): Result<List<Hadith>>
    fun observeHadith(): Flow<Result<List<Hadith>>>
}

interface AzkarRepository {
    suspend fun getAzkar(): Result<List<Azkar>>
    suspend fun getAzkarByCategory(category: String): Result<Azkar>
    suspend fun saveAzkar(azkar: Azkar): Result<Unit>
    fun observeAzkar(): Flow<Result<List<Azkar>>>
}

interface DuasRepository {
    suspend fun getAllDuas(): Result<List<Dua>>
    suspend fun getDuasByCategory(category: String): Result<List<Dua>>
    suspend fun searchDuas(query: String): Result<List<Dua>>
    suspend fun saveDua(dua: Dua): Result<Unit>
    fun observeDuas(): Flow<Result<List<Dua>>>
}

interface UserManagementRepository {
    suspend fun getAllAdminUsers(): Result<List<AdminUser>>
    suspend fun getAdminUser(uid: String): Result<AdminUser>
    suspend fun createAdminUser(user: AdminUser, password: String): Result<Unit>
    suspend fun updateAdminUser(user: AdminUser): Result<Unit>
    suspend fun deleteAdminUser(uid: String): Result<Unit>
    suspend fun updateUserRole(uid: String, role: String): Result<Unit>
    fun observeAdminUsers(): Flow<Result<List<AdminUser>>>
}

interface NotificationRepository {
    suspend fun sendNotification(notification: Notification): Result<Unit>
    suspend fun getAllNotifications(): Result<List<Notification>>
    suspend fun deleteNotification(id: String): Result<Unit>
    fun observeNotifications(): Flow<Result<List<Notification>>>
}

interface SyncRepository {
    suspend fun syncAll(): Result<Unit>
    suspend fun syncQuran(): Result<Unit>
    suspend fun syncHadith(): Result<Unit>
    suspend fun syncAzkar(): Result<Unit>
    suspend fun syncDuas(): Result<Unit>
    fun observeSyncProgress(): Flow<Int> // Returns percentage 0-100
}
