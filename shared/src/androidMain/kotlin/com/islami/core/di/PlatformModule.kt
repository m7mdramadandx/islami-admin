package com.islami.core.di

import com.islami.data.local.LocalDataSource
import com.islami.data.local.LocalDataSourceImpl
import com.islami.data.remote.firebase.FirebaseCrashlyticsClient
import com.islami.data.remote.firebase.FirebaseCrashlyticsClientImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Local Data Sources (Android Implementation)
    single<LocalDataSource> { LocalDataSourceImpl(androidContext()) }

    // Firebase Crashlytics
    single { Firebase.crashlytics }
    single<FirebaseCrashlyticsClient> { FirebaseCrashlyticsClientImpl(get()) }
}
