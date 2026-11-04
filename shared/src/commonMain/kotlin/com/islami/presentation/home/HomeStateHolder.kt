package com.islami.presentation.home

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.AppReleaseSnapshot
import com.islami.domain.usecases.release.LoadAppReleaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = true,
    /** What the mobile app currently reads from `root/version`; null if it couldn't be loaded. */
    val live: AppReleaseSnapshot? = null,
    val loadFailed: Boolean = false
)

/** Created fresh each time the dashboard opens, so the live numbers reflect edits just made. */
class HomeStateHolder(
    private val loadAppRelease: LoadAppReleaseUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<HomeState>(HomeState()) {

    init {
        refresh()
    }

    fun refresh() = scope.launch {
        updateState { it.copy(isLoading = true, loadFailed = false) }
        val result = loadAppRelease(NoParams)
        updateState {
            it.copy(
                isLoading = false,
                live = (result as? Result.Success)?.data ?: it.live,
                loadFailed = result is Result.Error
            )
        }
    }
}
