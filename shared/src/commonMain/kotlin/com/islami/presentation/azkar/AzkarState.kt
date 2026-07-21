package com.islami.presentation.azkar

import com.islami.domain.entities.Azkar

data class AzkarState(
    val isLoading: Boolean = false,
    val azkarList: List<Azkar> = emptyList(),
    val error: String? = null
)
