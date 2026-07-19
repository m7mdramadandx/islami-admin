package com.islami.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // iOS Firebase Clients - TODO: Implement GitLive Firebase SDK for iOS
    // iOS HTTP Client - TODO: Implement Ktor HTTP client for iOS
    // iOS Local Data Sources - TODO: Implement UserDefaults or Realm-based storage
}
