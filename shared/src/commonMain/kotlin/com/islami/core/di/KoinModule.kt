package com.islami.core.di

import com.islami.data.repository.AuthRepositoryImpl
import com.islami.domain.repositories.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule(): Module = module {
    // Repositories
    single<AuthRepository> {
        AuthRepositoryImpl(
            firebaseAuthClient = get(),
            localDataSource = get()
        )
    }
}

/**
 * Platform-specific module that must be defined in each platform
 */
expect fun platformModule(): Module

/**
 * Initialize all KMP modules
 * Call this from platform-specific entry points (Android, iOS, Web)
 */
fun initializeKoinModules(): List<Module> = listOf(
    commonModule(),
    platformModule()
)
