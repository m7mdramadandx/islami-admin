package com.islami.presentation.quran

import com.islami.domain.entities.Surah

data class QuranState(
    val isLoading: Boolean = false,
    val surahs: List<Surah> = emptyList(),
    val error: String? = null
)
