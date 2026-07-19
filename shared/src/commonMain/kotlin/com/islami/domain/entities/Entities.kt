package com.islami.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty() = User(uid = "", email = null)
    }
}

@Serializable
data class HomeStats(
    @SerialName("totalUsers")
    val totalUsers: Int,
    @SerialName("totalFeedback")
    val totalFeedback: Int,
    @SerialName("totalHadith")
    val totalHadith: Int,
    @SerialName("quranSurahs")
    val quranSurahs: Int,
    @SerialName("quranAyahs")
    val quranAyahs: Int,
    @SerialName("feedbackRatePerThousandUsers")
    val feedbackRatePerThousandUsers: Double,
    @SerialName("appVersion")
    val appVersion: String
)

@Serializable
data class FeedbackMessage(
    val id: String,
    @SerialName("app_version")
    val appVersion: String,
    val deviceName: String,
    val email: String,
    val phone: String,
    val date: String,
    val msg: String
)

@Serializable
data class Surah(
    val number: Int,
    val name: String,
    @SerialName("englishName")
    val englishName: String,
    val revelationType: String,
    val totalVerses: Int,
    val totalSajdahs: Int = 0
)

@Serializable
data class Ayah(
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String,
    val translation: String? = null,
    val tafsir: String? = null
)

@Serializable
data class Hadith(
    val id: String,
    val book: String,
    val hadithNumber: String,
    val arabicText: String,
    val englishText: String,
    val source: String? = null,
    val grade: String? = null
)

@Serializable
data class Azkar(
    val id: String,
    val category: String,
    val items: List<AzkarItem>
)

@Serializable
data class AzkarItem(
    val id: String,
    val text: String,
    val count: Int,
    val benefits: String? = null
)

@Serializable
data class Dua(
    val id: String,
    val category: String,
    val arabicText: String,
    val englishTranslation: String,
    val source: String? = null,
    val timing: String? = null
)

@Serializable
data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val role: String,
    val permissions: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long? = null,
    val isActive: Boolean = true
)

@Serializable
data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val sentAt: Long = System.currentTimeMillis(),
    val targetUsers: List<String>? = null,
    val targetDevices: List<String>? = null,
    val priority: String = "high"
)

@Serializable
data class AppSettings(
    val appVersion: String,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String? = null,
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val forceUpdateVersion: String? = null,
    val analyticsEnabled: Boolean = true
)
