# Islami Admin Panel

## Overview

This an admin panel for the Islami app. It provides a user-friendly interface for managing the app's content, including Hadiths, articles, and users. The panel is built with Flutter and uses Firebase for authentication and data storage.

## Features

- **Login:** Secure login for administrators.
- **Dashboard:** A central hub for accessing all management features.
- **Hadith Management:** Add, edit, and delete Hadiths.
- **User Management:** Manage app users.
- **Article Management:** Create, edit, and publish articles.
- **Category Management:** Organize articles into categories.
- **Comment Moderation:** Approve or reject user comments.

## Style and Design

- **Theme:** Modern, clean design with a consistent color scheme.
- **Typography:** Clear and readable fonts, with a focus on usability.
- **Layout:** Responsive layout that adapts to different screen sizes.
- **Components:** Custom widgets for a unique and intuitive user experience.

## Implemented Features

### General
- **App Title:** Changed the app title to `islami-admin` in `main.dart`, `web/index.html` and `web/manifest.json`
- **Firebase Crashlytics:** Implemented Firebase Crashlytics to automatically report crashes and errors.

### Authentication

- **Login Page:** Created a visually appealing login page with email and password fields.
- **Firebase Auth:** Integrated Firebase Authentication for secure email and password login.
- **Bloc Pattern:** Used the Bloc pattern for state management in the authentication process.

### Home Page

- **Dashboard:** Designed a modern dashboard with cards for accessing different management features.
- **Custom Drawer:** Implemented a custom drawer for easy navigation.

### Hadith Management

- **Hadith Model:** Defined the data structure for Hadiths, including `toJson` and `fromJson` methods for Firestore compatibility.
- **Firestore Integration:** Replaced the local data with a `HadithRepository` that uses Firebase Firestore for all CRUD operations (Create, Read, Update, Delete).
- **UI:** Created a user-friendly interface for listing, adding, editing, and deleting Hadiths, now powered by a `StreamBuilder` connected to Firestore.

### User Management

- **User Model:** Defined the data structure for users, including `toJson` and `fromJson` methods for Firestore compatibility.
- **Firestore Integration:** Replaced the local data with a `UserRepository` that uses Firebase Firestore for all CRUD operations.
- **UI:** Created a user-friendly interface for listing, editing, and deleting users, now powered by a `StreamBuilder` connected to Firestore.
