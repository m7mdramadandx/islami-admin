package com.islami.ramadan

import com.islami.core.error.Result
import com.islami.domain.entities.RamadanDay
import com.islami.domain.ramadan.changedFrom
import com.islami.domain.ramadan.ramadanDayName
import com.islami.domain.ramadan.validate
import com.islami.domain.repositories.RamadanRepository
import com.islami.domain.usecases.ramadan.SaveRamadanDaysParams
import com.islami.domain.usecases.ramadan.SaveRamadanDaysUseCase
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

    private class RecordingRepository : RamadanRepository {
        var saved: List<RamadanDay> = emptyList()
        override suspend fun loadDays(): Result<List<RamadanDay>> = Result.Success(emptyList())
        override suspend fun saveDays(changed: List<RamadanDay>, base: List<RamadanDay>, overwrite: Boolean): Result<List<RamadanDay>> {
            saved = changed
            return Result.Success(base)
        }
    }
}
