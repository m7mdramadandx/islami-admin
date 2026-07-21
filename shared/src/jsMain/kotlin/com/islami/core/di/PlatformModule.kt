package com.islami.core.di

import com.islami.data.local.LocalDataSource
import com.islami.data.local.LocalDataSourceImpl
import com.islami.data.remote.firebase.FirebaseCrashlyticsClient
import com.islami.data.remote.firebase.FirebaseCrashlyticsClientImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Firebase Crashlytics (No-op on JS)
    single<FirebaseCrashlyticsClient> { FirebaseCrashlyticsClientImpl() }
    
    // Local Data Source (JS Implementation)
    single<LocalDataSource> { LocalDataSourceImpl() }
}
