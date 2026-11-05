package com.islami.ramadan

import com.islami.core.error.Result
import com.islami.domain.entities.RamadanBackground
import com.islami.domain.entities.RamadanDay
import com.islami.domain.ramadan.BackgroundImageType
import com.islami.domain.ramadan.MAX_BACKGROUND_BYTES
import com.islami.domain.ramadan.backgroundUploadProblem
import com.islami.domain.ramadan.changedFrom
import com.islami.domain.ramadan.detectBackgroundType
import com.islami.domain.ramadan.ramadanDayName
import com.islami.domain.ramadan.storagePathOf
import com.islami.domain.ramadan.validate
import com.islami.domain.repositories.RamadanRepository
import com.islami.domain.usecases.ramadan.SaveRamadanDaysParams
import com.islami.domain.usecases.ramadan.SaveRamadanDaysUseCase
import com.islami.domain.usecases.ramadan.UploadRamadanBackgroundUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RamadanRulesTest {

    private fun day(n: Int, dua: String = "دعاء $n", bg: String = "https://x/o/islamic_pictures%2F$n.jpg?alt=media") =
        RamadanDay(n.toString().padStart(2, '0'), dua, bg)

    private val published = (1..30).map { day(it) }

    @Test
    fun liveShapedDataIsValid() {
        assertTrue(published.validate().none { it.isError })
    }

    @Test
    fun emptyDuaOnARealDayBlocksSaving() {
        val issues = published.map { if (it.dayNumber == 5) it.copy(dua = "  ") else it }.validate()
        assertEquals(listOf("05"), issues.filter { it.isError }.map { it.dayId })
    }

    @Test
    fun missingBackgroundOnlyWarns() {
        val issues = published.map { if (it.dayNumber == 2) it.copy(backgroundUrl = "") else it }.validate()
        assertEquals(1, issues.count { !it.isError && it.dayId == "02" })
        assertTrue(issues.none { it.isError })
    }

    @Test
    fun everyDayHasACorrectlySpelledTitle() {
        assertEquals(30, (1..30).mapNotNull { ramadanDayName(it) }.toSet().size)
        assertEquals("اليوم الأول", published.first().title)
        // The old database values had "الثالت", a trailing space on day 8 and "الواحد والعشرون".
        assertEquals("اليوم الثالث", ramadanDayName(3))
        assertEquals("اليوم الثامن", ramadanDayName(8))
        assertEquals("اليوم الحادي والعشرون", ramadanDayName(21))
        assertEquals(null, ramadanDayName(0))
        assertEquals(null, ramadanDayName(31))
    }

    @Test
    fun changesAreTrackedPerDay() {
        val edited = published.map { if (it.dayNumber in setOf(3, 7)) it.copy(dua = it.dua + "!") else it }
        assertEquals(setOf("03", "07"), edited.changedFrom(published))
    }

    @Test
    fun storagePathIsReadFromDownloadUrls() {
        val url = "https://firebasestorage.googleapis.com/v0/b/islami-ecc03.appspot.com/o/islamic_pictures%2F0c73.jpg?alt=media&token=9f13"
        assertEquals("islamic_pictures/0c73.jpg", storagePathOf(url))
        assertEquals(null, storagePathOf(""))
    }

    @Test
    fun saveSendsOnlyTheEditedDays() = runTest {
        val repo = RecordingRepository()
        val edited = published.map { if (it.dayNumber == 4) it.copy(dua = "جديد") else it }
        SaveRamadanDaysUseCase(repo)(SaveRamadanDaysParams(edited, published))
        assertEquals(listOf("04"), repo.saved.map { it.id })
    }

    @Test
    fun saveRefusesAnEmptyDua() = runTest {
        val repo = RecordingRepository()
        val edited = published.map { if (it.dayNumber == 4) it.copy(dua = "") else it }
        val result = SaveRamadanDaysUseCase(repo)(SaveRamadanDaysParams(edited, published))
        assertTrue(result is Result.Error)
        assertTrue(repo.saved.isEmpty())
    }

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    private val jpeg = bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0x10)
    private val png = bytes(0x89, 'P'.code, 'N'.code, 'G'.code, 0x0D, 0x0A)
    private val webp = "RIFF".encodeToByteArray() + ByteArray(4) + "WEBPVP8 ".encodeToByteArray()

    @Test
    fun detectsFormatsFromFileSignatures() {
        assertEquals(BackgroundImageType.JPEG, detectBackgroundType(jpeg))
        assertEquals(BackgroundImageType.PNG, detectBackgroundType(png))
        assertEquals(BackgroundImageType.WEBP, detectBackgroundType(webp))
        assertEquals(null, detectBackgroundType("GIF89a".encodeToByteArray()))
        assertEquals(null, detectBackgroundType(ByteArray(2)))
    }

    @Test
    fun rejectsEmptyUnknownAndOversizedFiles() {
        assertEquals(null, backgroundUploadProblem(jpeg))
        assertTrue(backgroundUploadProblem(ByteArray(0))!!.contains("empty"))
        assertTrue(backgroundUploadProblem("hello".encodeToByteArray())!!.contains("JPEG, PNG or WebP"))
        val big = jpeg + ByteArray(MAX_BACKGROUND_BYTES)
        assertTrue(backgroundUploadProblem(big)!!.contains("1.0 MB"))
    }

    @Test
    fun badFilesNeverReachStorage() = runTest {
        val repo = RecordingRepository()
        val result = UploadRamadanBackgroundUseCase(repo)("not an image".encodeToByteArray())
        assertTrue(result is Result.Error)
        assertEquals(null, repo.uploadedType)

        UploadRamadanBackgroundUseCase(repo)(png)
        assertEquals(BackgroundImageType.PNG, repo.uploadedType)
    }

    private class RecordingRepository : RamadanRepository {
        var saved: List<RamadanDay> = emptyList()
        var uploadedType: BackgroundImageType? = null
        override suspend fun loadDays(): Result<List<RamadanDay>> = Result.Success(emptyList())
        override suspend fun saveDays(changed: List<RamadanDay>, base: List<RamadanDay>, overwrite: Boolean): Result<List<RamadanDay>> {
            saved = changed
            return Result.Success(base)
        }
        override suspend fun listBackgrounds(): Result<List<RamadanBackground>> = Result.Success(emptyList())
        override suspend fun uploadBackground(bytes: ByteArray, type: BackgroundImageType): Result<RamadanBackground> {
            uploadedType = type
            return Result.Success(RamadanBackground("islamic_pictures/x.${type.extension}", "https://x"))
        }
    }
}
