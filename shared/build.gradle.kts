plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Web target (JS/WASM)
    wasmJs {
        browser()
    }

    sourceSets {
        // Shared source set for all platforms
        commonMain.dependencies {
            // Kotlin & Coroutines
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            // DI - Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // HTTP - Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content_negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            // Firebase (GitLive wrappers - KMP compatible)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.storage)
            implementation(libs.gitlive.firebase.crashlytics)

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.truth)
        }

        // Android-specific dependencies
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)

            // AndroidX
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)

            // Room for offline persistence
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
        }

        // iOS-specific dependencies
        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
        }

        // Web-specific dependencies
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "com.islami.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
