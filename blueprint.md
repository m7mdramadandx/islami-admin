
# Project Blueprint

## Overview

This document outlines the style, design, and features of the Islami Admin Flutter application. It serves as a single source of truth for the application's architecture and capabilities.

## Style and Design

- **Theme**: Material 3 with a consistent color scheme and typography.
- **Color Palette**: Primary color seeded from `Colors.deepPurple`.
- **Typography**: `google_fonts` package is used for custom fonts (`Oswald`, `Roboto`, `Open Sans`).
- **Layout**: Clean, responsive, and mobile-first design.
- **Iconography**: `flutter_svg` is used for scalable and high-quality vector graphics.

## Existing Features

- **Authentication**: Firebase Auth for user sign-in.
- **Quran**: Feature to browse and read the Quran.
- **Notifications**:
  - A UI to manage and send FCM notifications.
  - A backend Cloud Function (`sendFcmNotification`) to handle the message sending logic.

## Current Task: Azkar Feature

### Plan

1.  **Add Dependencies**: Add `firebase_storage` to `pubspec.yaml` for file operations in Firebase Storage.
2.  **Create Feature Structure**:
    - Create a new feature directory: `lib/features/azkar`.
    - Set up the standard BLoC architecture:
        - `data`: `datasources` and `repositories`.
        - `domain`: `repositories` and `usecases`.
        - `presentation`: `bloc` and `pages`.
3.  **Implement Data Layer**:
    - **`AzkarRemoteDataSource`**:
        - A method to download the `azkar.json` file from `gs://islami-ecc03.appspot.com/json/azkar.json`.
        - A method to upload the modified JSON string back to the same path, overwriting the existing file.
4.  **Implement Domain Layer**:
    - **`AzkarRepository`**: An abstract class defining the contract for fetching and saving Azkar data.
    - **Usecases**: `GetAzkar` and `SaveAzkar` to handle the business logic.
5.  **Implement Presentation Layer (BLoC & UI)**:
    - **`AzkarBloc`**: Manage the state of the feature (`AzkarInitial`, `AzkarLoading`, `AzkarLoaded`, `AzkarSaving`, `AzkarError`).
    - **`AzkarPage`**:
        - A `Scaffold` with an `AppBar`.
        - A `BlocBuilder` to react to state changes.
        - Display a `CircularProgressIndicator` while loading or saving.
        - Use a `TextField` with a `TextEditingController` to display and edit the fetched JSON content.
        - An `IconButton` in the `AppBar` to trigger the save event.
        - The page will automatically fetch the data on load.
6.  **Integrate Navigation**:
    - Add a new `GoRoute` for `/azkar` in the main router configuration.
    - Add a navigation element (e.g., a button or a list tile) in the UI to navigate to the new Azkar page.

