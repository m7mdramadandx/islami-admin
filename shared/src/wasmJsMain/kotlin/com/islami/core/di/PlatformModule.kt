package com.islami.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Web/WASM Firebase Clients - TODO: Implement GitLive Firebase SDK for WASM
    // Web/WASM HTTP Client - TODO: Implement Ktor HTTP client for WASM
    // Web/WASM Local Data Sources - TODO: Implement localStorage-based storage
}
