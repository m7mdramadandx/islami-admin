package com.islami.core.di

import com.islami.data.local.ContentDraftStore
import com.islami.data.remote.RemoteImageLoader
import com.islami.data.remote.firebase.*
import com.islami.data.repository.*
import com.islami.domain.entities.ContentKind
import com.islami.domain.repositories.*
import com.islami.domain.usecases.auth.*
import com.islami.domain.usecases.feedback.*
import com.islami.domain.usecases.content.*
import com.islami.domain.usecases.release.*
import com.islami.domain.usecases.ramadan.*
import com.islami.domain.usecases.notifications.*
import com.islami.presentation.auth.*
import com.islami.presentation.home.*
import com.islami.presentation.feedback.*
import com.islami.presentation.notifications.*
import com.islami.presentation.content.*
import com.islami.presentation.release.*
import com.islami.presentation.ramadan.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.storage.storage
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule(): Module = module {
    // Firebase SDK (Common)
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }
    single { Firebase.functions }

    // Firebase Clients (Wrappers)
    single<FirebaseAuthClient> { FirebaseAuthClientImpl(get()) }
    single<FirebaseFirestoreClient> { FirebaseFirestoreClientImpl(get()) }
    single<FirebaseStorageClient> { FirebaseStorageClientImpl(get()) }
    single { RemoteImageLoader() }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<FeedbackRepository> { FeedbackRepositoryImpl(firestore = get()) }
    single { ContentDraftStore(get()) }
    single<RamadanRepository> { RamadanRepositoryImpl(firestore = get(), storage = get()) }
    single<AppReleaseRepository> { AppReleaseRepositoryImpl(firestore = get(), firestoreClient = get(), auth = get()) }
    single<ContentRepository> { ContentRepositoryImpl(storage = get(), firestore = get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get()) }

    // Use Cases
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { GetCurrentUserUseCase(get()) }
    single { ObserveFeedbackMessagesUseCase(get()) }
    single { DeleteFeedbackMessageUseCase(get()) }
    single { LoadRamadanDaysUseCase(get()) }
    single { SaveRamadanDaysUseCase(get()) }
    single { ListRamadanBackgroundsUseCase(get()) }
    single { UploadRamadanBackgroundUseCase(get()) }
    single { LoadAppReleaseUseCase(get()) }
    single { SaveAppReleaseUseCase(get()) }
    single { ListAppReleaseHistoryUseCase(get()) }
    single { LoadContentUseCase(get()) }
    single { PublishContentUseCase(get()) }
    single { BumpContentVersionUseCase(get()) }
    single { ListContentBackupsUseCase(get()) }
    single { LoadContentBackupUseCase(get()) }
    single { SendNotificationUseCase(get()) }

    // State Holders (ViewModels)
    single { SessionStateHolder(get(), get()) }
    factory { LoginStateHolder(get()) }
    factory { HomeStateHolder(get()) }
    factory { FeedbackStateHolder(get(), get()) }
    factory { NotificationStateHolder(get()) }
    factory { AppReleaseStateHolder(get(), get(), get(), get()) }
    factory { RamadanStateHolder(get(), get(), get(), get()) }
    // One editor per file; the screen keeps both alive so switching tabs keeps unsaved edits.
    factory { (kind: ContentKind) -> ContentEditorStateHolder(kind, get(), get(), get(), get(), get(), get()) }
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
