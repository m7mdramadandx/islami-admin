package com.islami.presentation.azkar

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.usecases.azkar.GetAzkarUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AzkarStateHolder(
    private val getAzkarUseCase: GetAzkarUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<AzkarState>(AzkarState()) {

    init {
        loadAzkar()
    }

    fun loadAzkar() = scope.launch {
        updateState { it.copy(isLoading = true, error = null) }
        val result = getAzkarUseCase(NoParams)
        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, azkarList = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }
    }
}
