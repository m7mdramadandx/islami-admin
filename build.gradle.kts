plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.crashlytics).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
}


/* Root clean task is provided by Kotlin/JS plugin or can be added manually if needed */
