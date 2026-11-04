package com.islami.domain.usecases.release

import com.islami.domain.entities.AppReleaseConfig

data class SaveAppReleaseParams(
    val config: AppReleaseConfig,
    val base: AppReleaseConfig,
    /** Save even though someone else changed the live config since [base] was loaded. */
    val overwrite: Boolean = false
)
