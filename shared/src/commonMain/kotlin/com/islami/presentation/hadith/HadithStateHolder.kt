package com.islami.presentation.hadith

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.usecases.hadith.GetAllHadithUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class HadithStateHolder(
    private val getAllHadithUseCase: GetAllHadithUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : StateHolder<HadithState>(HadithState()) {

    init {
        loadHadith()
    }

    fun loadHadith() = scope.launch {
        updateState { it.copy(isLoading = true, error = null) }
        val result = getAllHadithUseCase(NoParams)
        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, hadiths = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }
    }
}
