package com.islami.presentation.quran

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.usecases.quran.GetAllSurahsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class QuranStateHolder(
    private val getAllSurahsUseCase: GetAllSurahsUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : StateHolder<QuranState>(QuranState()) {

    init {
        loadSurahs()
    }

    fun loadSurahs() = scope.launch {
        updateState { it.copy(isLoading = true, error = null) }
        val result = getAllSurahsUseCase(NoParams)
        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, surahs = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }
    }
}
