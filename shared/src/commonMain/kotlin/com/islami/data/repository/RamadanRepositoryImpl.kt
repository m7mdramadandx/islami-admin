package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.domain.entities.RamadanConflictException
import com.islami.domain.entities.RamadanDay
import com.islami.domain.repositories.RamadanRepository
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.cancellation.CancellationException

/** Edits `root/ar/ramadan`, writing only `dua` and `backgroundUrl`; day titles live in code. */
class RamadanRepositoryImpl(
    private val firestore: FirebaseFirestore
) : RamadanRepository {

    override suspend fun loadDays(): Result<List<RamadanDay>> = attempt { readDays() }

    override suspend fun saveDays(
        changed: List<RamadanDay>,
        base: List<RamadanDay>,
        overwrite: Boolean
    ): Result<List<RamadanDay>> = attempt {
        if (!overwrite) {
            val live = readDays().associateBy { it.id }
            val before = base.associateBy { it.id }
            val conflicts = changed.filter { live[it.id] != before[it.id] }.map { it.dayNumber }
            if (conflicts.isNotEmpty()) throw RamadanConflictException(conflicts)
        }

        val batch = firestore.batch()
        changed.forEach { day ->
            batch.updateFields(collection().document(day.id)) {
                DUA to day.dua
                BACKGROUND_URL to day.backgroundUrl
            }
        }
        batch.commit()
        readDays()
    }

    // Firestore returns documents in id order, which is the order the app indexes days by.
    private suspend fun readDays(): List<RamadanDay> = collection().get().documents.map { doc ->
        RamadanDay(
            id = doc.id,
            dua = doc.string(DUA),
            backgroundUrl = doc.string(BACKGROUND_URL)
        )
    }.sortedBy { it.id }

    private fun DocumentSnapshot.string(field: String): String =
        if (!contains(field)) "" else runCatching { get<String?>(field) }.getOrNull().orEmpty()

    private fun collection() = firestore.collection(COLLECTION)

    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }

    private companion object {
        const val COLLECTION = "root/ar/ramadan"
        const val DUA = "dua"
        const val BACKGROUND_URL = "backgroundUrl"
    }
}
