package com.islami.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule(): Module = module {
    // Repositories will be added in Phase 2
    // DataSources will be added in Phase 2
    // UseCases will be added in Phase 3
}

/**
 * Initialize all KMP modules
 * Call this from platform-specific entry points (Android, iOS, Web)
 */
fun initializeKoinModules(): List<Module> = listOf(
    commonModule(),
    // Additional modules will be added in later phases
)
