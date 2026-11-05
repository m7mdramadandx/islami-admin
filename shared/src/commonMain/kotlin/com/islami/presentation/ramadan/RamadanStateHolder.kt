package com.islami.presentation.ramadan

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.RamadanConflictException
import com.islami.domain.entities.ImageFolder
import com.islami.domain.entities.RamadanDay
import com.islami.domain.usecases.images.ListImagesUseCase
import com.islami.domain.usecases.images.UploadImageParams
import com.islami.domain.usecases.images.UploadImageUseCase
import com.islami.domain.ramadan.changedFrom
import com.islami.domain.ramadan.validate
import com.islami.domain.usecases.ramadan.LoadRamadanDaysUseCase
import com.islami.domain.usecases.ramadan.SaveRamadanDaysParams
import com.islami.domain.usecases.ramadan.SaveRamadanDaysUseCase
import com.islami.presentation.components.PickedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RamadanStateHolder(
    private val loadDays: LoadRamadanDaysUseCase,
    private val saveDays: SaveRamadanDaysUseCase,
    private val listImages: ListImagesUseCase,
    private val uploadImage: UploadImageUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<RamadanState>(RamadanState()) {

    init {
        load()
    }

    fun load() = scope.launch {
        updateState { it.copy(isLoading = true, loadError = null) }
        when (val result = loadDays(NoParams)) {
            is Result.Success -> updateState { state ->
                val days = result.data
                state.copy(
                    isLoading = false,
                    published = days,
                    selectedId = state.selectedId?.takeIf { id -> days.any { it.id == id } }
                        ?: days.firstOrNull()?.id
                ).withDraft(days)
            }
            is Result.Error -> updateState { it.copy(isLoading = false, loadError = result.exception.readable()) }
            is Result.Loading -> Unit
        }
    }

    fun select(id: String) = updateState { it.copy(selectedId = id) }

    fun onDuaChanged(value: String) = editSelected { it.copy(dua = value) }

    // region Backgrounds

    fun openPicker() {
        updateState { it.copy(pickerOpen = true) }
        if (currentState().backgrounds == null) loadBackgrounds()
    }

    fun dismissPicker() = updateState { it.copy(pickerOpen = false) }

    fun loadBackgrounds() = scope.launch {
        updateState { it.copy(isLoadingBackgrounds = true) }
        when (val result = listImages(ImageFolder.RAMADAN_BACKGROUNDS)) {
            is Result.Success -> updateState { it.copy(isLoadingBackgrounds = false, backgrounds = result.data) }
            is Result.Error -> updateState {
                it.copy(isLoadingBackgrounds = false, message = "Couldn't list images: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    /** Uploads the picked file and makes it the selected day's background (saved with the day). */
    fun onImagePicked(image: PickedImage?) {
        if (image == null) {
            updateState { it.copy(message = "Couldn't read that file") }
            return
        }
        // The admin may pick another day while this uploads; the image belongs to this one.
        val day = currentState().selected ?: return
        updateState { it.copy(isUploading = true) }
        scope.launch {
            when (val result = uploadImage(UploadImageParams(ImageFolder.RAMADAN_BACKGROUNDS, image.bytes))) {
                is Result.Success -> {
                    val background = result.data
                    editDay(day.id) { it.copy(backgroundUrl = background.url) }
                    updateState {
                        it.copy(
                            isUploading = false,
                            backgrounds = it.backgrounds?.let { list -> list + background },
                            message = "Uploaded and set as day ${day.dayNumber}'s background. Save to publish it"
                        )
                    }
                }
                is Result.Error -> updateState {
                    it.copy(isUploading = false, message = "Upload failed: ${result.exception.readable()}")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun chooseBackground(url: String) = editSelected { it.copy(backgroundUrl = url) }

    // endregion

    fun discardChanges() = updateState { it.withDraft(it.published) }

    // region Saving

    fun requestSave() {
        val state = currentState()
        val firstError = state.issues.firstOrNull { it.isError }
        if (firstError != null) {
            updateState { it.copy(selectedId = firstError.dayId, message = firstError.message) }
            return
        }
        if (state.hasChanges && !state.isSaving) updateState { it.copy(confirmSave = true) }
    }

    fun dismissSave() = updateState { it.copy(confirmSave = false, conflictDays = emptyList()) }

    fun confirmSave(overwrite: Boolean = false) {
        val state = currentState()
        val draft = state.draft
        updateState { it.copy(confirmSave = false, conflictDays = emptyList(), isSaving = true) }
        scope.launch {
            when (val result = saveDays(SaveRamadanDaysParams(draft, state.published, overwrite))) {
                is Result.Success -> updateState {
                    // Keep edits made while saving; they show as unsaved against the new published data.
                    it.copy(isSaving = false, published = result.data, message = "Saved. The app shows it right away")
                        .withDraft(it.draft)
                }
                is Result.Error -> when (val e = result.exception) {
                    is RamadanConflictException -> updateState { it.copy(isSaving = false, conflictDays = e.days) }
                    else -> updateState { it.copy(isSaving = false, message = "Save failed: ${e.readable()}") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // endregion

    fun onMessageShown() = updateState { it.copy(message = null) }

    private fun editSelected(transform: (RamadanDay) -> RamadanDay) {
        editDay(currentState().selectedId ?: return, transform)
    }

    private fun editDay(id: String, transform: (RamadanDay) -> RamadanDay) = updateState { state ->
        state.withDraft(state.draft.map { if (it.id == id) transform(it) else it })
    }

    private fun RamadanState.withDraft(draft: List<RamadanDay>) = copy(
        draft = draft,
        issues = draft.validate(),
        changedIds = draft.changedFrom(published)
    )

    private fun Exception.readable(): String = message?.takeIf { it.isNotBlank() } ?: "Unknown error"
}
