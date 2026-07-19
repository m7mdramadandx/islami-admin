package com.islami.core.di

import android.content.Context
import com.islami.data.local.LocalDataSource
import com.islami.data.local.LocalDataSourceImpl
import com.islami.data.remote.crashlytics.FirebaseCrashlyticsClientImpl
import com.islami.data.remote.firebase.FirebaseAuthClient
import com.islami.data.remote.firebase.FirebaseAuthClientImpl
import com.islami.data.remote.firebase.FirebaseCrashlyticsClient
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.data.remote.firebase.FirebaseStorageClient
import com.islami.data.remote.firestore.FirebaseFirestoreClientImpl
import com.islami.data.remote.http.HttpClient
import com.islami.data.remote.http.HttpClientImpl
import com.islami.data.remote.storage.FirebaseStorageClientImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Android Context
    val context: Context = androidContext()

    // Firebase Auth Client
    single<FirebaseAuthClient> { FirebaseAuthClientImpl() }

    // Firebase Firestore Client
    single<FirebaseFirestoreClient> { FirebaseFirestoreClientImpl() }

    // Firebase Storage Client
    single<FirebaseStorageClient> { FirebaseStorageClientImpl() }

    // Firebase Crashlytics Client
    single<FirebaseCrashlyticsClient> { FirebaseCrashlyticsClientImpl() }

    // HTTP Client
    single<HttpClient> { HttpClientImpl() }

    // Local Data Sources
    single<LocalDataSource> { LocalDataSourceImpl(context) }
}
