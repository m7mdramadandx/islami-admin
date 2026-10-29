package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.domain.entities.FeedbackMessage
import com.islami.domain.repositories.FeedbackRepository
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

/** Feedback sent from the Islami app lives in `root/feedback/messages`. */
class FeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FeedbackRepository {

    private val messages: CollectionReference
        get() = firestore.collection("root").document("feedback").collection("messages")

    override fun observeFeedbackMessages(): Flow<Result<List<FeedbackMessage>>> =
        messages.snapshots
            .map<QuerySnapshot, Result<List<FeedbackMessage>>> { snapshot ->
                Result.Success(snapshot.documents.map { it.toFeedbackMessage() })
            }
            .catch { e -> emit(Result.Error(e as? Exception ?: Exception(e.message, e))) }

    override suspend fun deleteFeedbackMessage(id: String): Result<Unit> = try {
        messages.document(id).delete()
        Result.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }
}

// Missing fields stay blank; the UI decides how to present them.
private fun DocumentSnapshot.toFeedbackMessage() = FeedbackMessage(
    id = id,
    appVersion = textField("app_version") ?: "",
    deviceName = textField("deviceName") ?: textField("device_name") ?: "",
    email = textField("email") ?: "",
    phone = textField("phone") ?: "",
    date = textField("date") ?: "",
    msg = textField("msg") ?: ""
)

/**
 * Reads a field as text whatever its stored type, so one document with an
 * unexpected type (e.g. a numeric phone) can't fail the whole list.
 */
private fun DocumentSnapshot.textField(name: String): String? {
    if (!contains(name)) return null
    runCatching { get<String?>(name) }.getOrNull()?.let { return it }
    runCatching { get<Long?>(name) }.getOrNull()?.let { return it.toString() }
    runCatching { get<Double?>(name) }.getOrNull()?.let { return it.toString() }
    runCatching { get<Boolean?>(name) }.getOrNull()?.let { return it.toString() }
    return null
}
