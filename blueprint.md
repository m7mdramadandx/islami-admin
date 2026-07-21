# Project Blueprint - Islami Admin KMP

## Overview

This document outlines the style, design, and features of the Islami Admin Kotlin Multiplatform (KMP) application. It serves as a single source of truth for the application's architecture and capabilities. The project has been fully migrated from Flutter to KMP.

## Architecture

The application follows Clean Architecture principles with a shared module for business logic and UI.

### Layers:
1.  **Domain Layer (Shared)**: Entities, Repository interfaces, and Use Cases.
2.  **Data Layer (Shared)**: Repository implementations, Remote Data Sources (Firebase, Ktor), and Local Data Sources (DataStore).
3.  **Presentation Layer (Shared)**: MVI pattern using `StateHolder` and `EventStateHolder`. UI implemented using Compose Multiplatform.

### Technology Stack:
- **Language**: Kotlin 1.9.20
- **UI Framework**: Compose Multiplatform (Android & Web WASM)
- **Dependency Injection**: Koin
- **Networking**: Ktor Client
- **Database/Cloud**: GitLive Firebase SDK (Auth, Firestore, Storage, Crashlytics)
- **Serialization**: KotlinX Serialization
- **State Management**: Coroutines + Flow

## Style and Design

- **Theme**: Material 3 (Shared via `IslamiTheme`).
- **Color Palette**: Material 3 ColorScheme (seeded from deep purple/lavender tones).
- **Typography**: Shared Material3 Typography.
- **Layout**: Responsive grid and list layouts for cross-platform support.

## Existing Features

- **Authentication**: Shared Login screen with Firebase Auth.
- **Dashboard**: Real-time stats from Firestore.
- **Quran Management**: Browse and manage Surahs and Ayahs.
- **Hadith Management**: Browse and manage Hadith collections.
- **Azkar Management**: Browse and manage Azkar categories.
- **Feedback System**: View and respond to user feedback.
- **Notifications**: Send FCM notifications to users.

## Navigation Structure

A shared custom `Navigator` handles transitions between:
- `Login`: Authentication entry point.
- `Home`: Main dashboard.
- `Quran`: Surah management.
- `Hadith`: Hadith management.
- `Feedback`: User response system.
- `Notifications`: FCM broadcast utility.
- `Azkar`: Azkar content management.
