package com.islami.presentation.home

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.usecases.home.GetHomeStatsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class HomeStateHolder(
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : StateHolder<HomeState>(HomeState()) {

    init {
        loadStats()
    }

    fun loadStats() = scope.launch {
        updateState { it.copy(isLoading = true, error = null) }
        
        val result = getHomeStatsUseCase(NoParams)
        
        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, stats = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }
    }
}
