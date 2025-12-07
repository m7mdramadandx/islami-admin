# Islami Admin Panel

## Overview

This project is an admin panel for the Islami app. It provides a user interface for managing the app's content and users. The admin panel is built with Flutter and uses Firebase for authentication and data storage.

## Features

*   **Authentication:** Admins can log in with their email and password.
*   **Dashboard:** A central hub for managing the app.
*   **User Management:** View and manage users.
*   **Content Management:** Add, edit, and delete content.

## Project Structure

The project is organized into the following directories:

*   `lib/core`: Core components, such as error handling and use cases.
*   `lib/features`: Feature-based modules, such as authentication and dashboard.
*   `lib/injection_container.dart`: Dependency injection setup.
*   `lib/main.dart`: The main application entry point.

## Getting Started

To run the project, you will need to have Flutter and Firebase installed. Once you have set up your environment, you can run the following command to start the app:

```
flutter run
```

## Current Task

Refactor the project to use a more organized folder structure and replace the dashboard with a home page.
