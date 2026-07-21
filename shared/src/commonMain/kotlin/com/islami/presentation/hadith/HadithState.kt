package com.islami.presentation.hadith

import com.islami.domain.entities.Hadith

data class HadithState(
    val isLoading: Boolean = false,
    val hadiths: List<Hadith> = emptyList(),
    val error: String? = null
)
