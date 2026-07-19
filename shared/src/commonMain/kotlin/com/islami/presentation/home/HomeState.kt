package com.islami.presentation.home

import com.islami.domain.entities.HomeStats

data class HomeState(
    val isLoading: Boolean = false,
    val stats: HomeStats? = null,
    val error: String? = null
)
