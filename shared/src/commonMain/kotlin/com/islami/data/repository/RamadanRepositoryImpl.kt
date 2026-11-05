package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseStorageClient
import com.islami.domain.entities.RamadanBackground
import com.islami.domain.entities.RamadanConflictException
import com.islami.domain.entities.RamadanDay
import com.islami.domain.ramadan.BackgroundImageType
import com.islami.domain.repositories.RamadanRepository
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

/**
 * Edits `root/ar/ramadan`. The app reads `root/ar` for every language, so this is the only copy.
 * Only `dua` and `backgroundUrl` are written, as a field update, never a document replace. Day
 * titles come from a fixed map in code (see ramadanDayName), so any `dayName` left in a document
 * is neither read nor written.
 */
class RamadanRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorageClient
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

    override suspend fun listBackgrounds(): Result<List<RamadanBackground>> = attempt {
        val paths = storage.list(BACKGROUNDS_FOLDER).orThrow()
        coroutineScope {
            paths.sorted().map { path ->
                async {
                    (storage.downloadUrl(path) as? Result.Success)?.data?.let { RamadanBackground(path.removePrefix("/"), it) }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun uploadBackground(bytes: ByteArray, type: BackgroundImageType): Result<RamadanBackground> = attempt {
        // Random 32-hex names, like the existing images, so uploads never overwrite one another.
        val name = (1..32).joinToString("") { Random.nextInt(16).toString(16) }
        val path = "$BACKGROUNDS_FOLDER/$name.${type.extension}"
        storage.upload(path, bytes, type.contentType).orThrow()
        RamadanBackground(path, storage.downloadUrl(path).orThrow())
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

    private fun <T> Result<T>.orThrow(): T = when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception
        is Result.Loading -> error("Unexpected loading result")
    }

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
        const val BACKGROUNDS_FOLDER = "islamic_pictures"
        const val DUA = "dua"
        const val BACKGROUND_URL = "backgroundUrl"
    }
}
