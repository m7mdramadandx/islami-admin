package com.islami.core.di

import com.islami.data.remote.firebase.FirebaseCrashlyticsClient
import com.islami.data.remote.firebase.FirebaseCrashlyticsClientImpl
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Firebase Crashlytics
    single { Firebase.crashlytics }
    single<FirebaseCrashlyticsClient> { FirebaseCrashlyticsClientImpl(get()) }
    
    // TODO: Implement iOS Local Data Source
}
