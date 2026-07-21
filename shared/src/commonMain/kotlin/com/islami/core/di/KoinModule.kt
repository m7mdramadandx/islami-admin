package com.islami.core.di

import com.islami.data.remote.firebase.*
import com.islami.data.remote.http.HttpClient
import com.islami.data.remote.http.HttpClientImpl
import com.islami.data.repository.*
import com.islami.domain.repositories.*
import com.islami.domain.usecases.auth.*
import com.islami.domain.usecases.home.*
import com.islami.domain.usecases.quran.*
import com.islami.domain.usecases.feedback.*
import com.islami.domain.usecases.hadith.*
import com.islami.domain.usecases.azkar.*
import com.islami.domain.usecases.notifications.*
import com.islami.presentation.auth.*
import com.islami.presentation.home.*
import com.islami.presentation.feedback.*
import com.islami.presentation.quran.*
import com.islami.presentation.hadith.*
import com.islami.presentation.notifications.*
import com.islami.presentation.azkar.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.crashlytics.crashlytics
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule(): Module = module {
    // Firebase SDK
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }
    single { Firebase.crashlytics }

    // Firebase Clients (Wrappers)
    single<FirebaseAuthClient> { FirebaseAuthClientImpl(get()) }
    single<FirebaseFirestoreClient> { FirebaseFirestoreClientImpl(get()) }
    single<FirebaseStorageClient> { FirebaseStorageClientImpl(get()) }
    single<FirebaseCrashlyticsClient> { FirebaseCrashlyticsClientImpl(get()) }

    // HTTP Client
    single<HttpClient> { HttpClientImpl() }

    // Repositories
    single<AuthRepository> {
        AuthRepositoryImpl(
            firebaseAuthClient = get(),
            localDataSource = get()
        )
    }
    single<HomeRepository> { HomeRepositoryImpl(get()) }
    single<FeedbackRepository> { FeedbackRepositoryImpl(get()) }
    single<QuranRepository> { QuranRepositoryImpl(get()) }
    single<HadithRepository> { HadithRepositoryImpl(get()) }
    single<AzkarRepository> { AzkarRepositoryImpl(get()) }
    single<DuasRepository> { DuasRepositoryImpl(get()) }
    single<UserManagementRepository> { UserManagementRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<SyncRepository> {
        SyncRepositoryImpl(
            httpClient = get(),
            quranRepository = get(),
            hadithRepository = get(),
            azkarRepository = get(),
            duasRepository = get()
        )
    }

    // Use Cases
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { GetCurrentUserUseCase(get()) }
    single { GetHomeStatsUseCase(get()) }
    single { GetAppSettingsUseCase(get()) }
    single { GetAllSurahsUseCase(get()) }
    single { GetSurahAyahsUseCase(get()) }
    single { GetFeedbackMessagesUseCase(get()) }
    single { RespondToFeedbackUseCase(get()) }
    single { GetAllHadithUseCase(get()) }
    single { GetAzkarUseCase(get()) }
    single { SendNotificationUseCase(get()) }

    // State Holders (ViewModels)
    factory { LoginStateHolder(get()) }
    factory { HomeStateHolder(get()) }
    factory { FeedbackStateHolder(get(), get()) }
    factory { QuranStateHolder(get()) }
    factory { HadithStateHolder(get()) }
    factory { NotificationStateHolder(get()) }
    factory { AzkarStateHolder(get()) }
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
