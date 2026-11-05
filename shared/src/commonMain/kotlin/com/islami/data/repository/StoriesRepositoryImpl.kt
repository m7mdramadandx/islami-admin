package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.core.time.currentTimeMillis
import com.islami.data.remote.firebase.FirebaseStorageClient
import com.islami.domain.entities.Story
import com.islami.domain.entities.StoryConflictException
import com.islami.domain.entities.StoryExistsException
import com.islami.domain.repositories.StoriesRepository
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/** Edits `root/ar/stories`; every save and delete first backs up to Storage `stories/backups/`. */
class StoriesRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorageClient
) : StoriesRepository {

    override suspend fun loadStories(): Result<List<Story>> = attempt {
        // Document-id order, the same order the app lists them in.
        collection().get().documents.map { it.toStory() }.sortedBy { it.id }
    }

    override suspend fun saveStory(story: Story, base: Story, overwrite: Boolean): Result<Story> = attempt {
        val live = collection().document(story.id).get()
        if (!overwrite && (!live.exists || live.toStory() != base)) throw StoryConflictException()
        if (!live.exists) {
            // Deleted by someone else, and the admin chose to keep their version: put it back.
            return@attempt createStory(story).let { (it as? Result.Error)?.exception?.let { e -> throw e }; story }
        }

        backUp(live.toStory())
        collection().document(story.id).updateFields {
            TITLE to story.title
            BRIEF to story.brief
            IMAGE to story.image
            BODY to story.body
        }
        story
    }

    override suspend fun createStory(story: Story): Result<Story> = attempt {
        val ref = collection().document(story.id)
        if (ref.get().exists) throw StoryExistsException(story.title)
        ref.set(
            NewStoryDocument.serializer(),
            NewStoryDocument(title = story.title, brief = story.brief, image = story.image, body = story.body)
        )
        story
    }

    override suspend fun deleteStory(story: Story): Result<Unit> = attempt {
        val live = collection().document(story.id).get()
        if (!live.exists) return@attempt
        if (live.toStory() != story) throw StoryConflictException()
        backUp(live.toStory())
        collection().document(story.id).delete()
    }

    private suspend fun backUp(story: Story) {
        val json = backupJson.encodeToString(StoryBackup.serializer(), StoryBackup(story.id, story.title, story.brief, story.image, story.body))
        val result = storage.upload("stories/backups/${story.id.trim()}/${currentTimeMillis()}.json", json.encodeToByteArray(), "application/json")
        // Never change a story without its backup in place.
        if (result is Result.Error) throw Exception("Couldn't back up the story first: ${result.exception.message}", result.exception)
    }

    private fun DocumentSnapshot.toStory() = Story(
        id = id,
        title = string(TITLE),
        brief = string(BRIEF),
        image = string(IMAGE),
        body = string(BODY)
    )

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

    /** Same shape as the existing documents, including the fields the app no longer reads. */
    @Serializable
    private data class NewStoryDocument(
        val title: String,
        val brief: String,
        val image: String,
        val body: String,
        val content: String = "",
        val videoID: String = "",
        val views: Int = 0,
        val text: List<String> = emptyList()
    )

    @Serializable
    private data class StoryBackup(val id: String, val title: String, val brief: String, val image: String, val body: String)

    private companion object {
        const val COLLECTION = "root/ar/stories"
        const val TITLE = "title"
        const val BRIEF = "brief"
        const val IMAGE = "image"
        const val BODY = "body"
        val backupJson = Json { prettyPrint = true }
    }
}
