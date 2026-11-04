# Islami Admin

Admin dashboard for the Islami mobile app, built with Kotlin Multiplatform and Compose Multiplatform.
It runs on the web and Android and works directly against the app's Firebase project (`islami-ecc03`).

## Features

- **Azkar & Duas**: edit `json/azkar.json` and `json/duas.json` in Storage, the files the app
  downloads. Publishing backs up the live file to `json/backups/`, then bumps `azkarVersion` /
  `duasVersion` in `root/version` so apps fetch the update.
- **Notifications**: send FCM pushes to a topic or one device through the `sendFcmNotification`
  Cloud Function (`functions/`).
- **Feedback**: read and delete messages users send from the app.
- **App Releases**: edit `storeVersion`, `minVersion` and release notes in `root/version`. Changes
  are recorded in `root/version/history`.

## Project layout

- `shared/`: all app code (domain, data, Compose UI), shared by every platform.
- `web/`: browser entry point (Kotlin/JS).
- `android/`: Android entry point.
- `functions/`: Firebase Cloud Functions.
- `cors.json`: Storage bucket CORS config; the web app needs it to download the JSON files.

## Running

```sh
# Web, at http://localhost:8080 (the port is fixed so the saved login survives restarts)
./gradlew :web:jsBrowserDevelopmentRun

# Android
./gradlew :android:installDebug

# Tests (the Compose check otherwise blocks plain JS logic tests)
./gradlew :shared:jsBrowserTest -x checkComposeUiTestConfigurationForJs
```

## Deploying the web dashboard

Firebase Hosting serves the production web build from `web/build/dist/js/productionExecutable`,
so always build right before deploying:

```sh
./gradlew :web:jsBrowserDistribution
firebase deploy --only hosting
```

Source maps are not uploaded. Every file is sent with `Cache-Control: no-cache` because the bundle
names (`islami-admin.js`, `skiko.wasm`) don't change between builds; browsers still cache them but
check for a new version on each load.

## Notification sending IAM fix

If notification sending fails with:
`Permission 'cloudmessaging.messages.create' denied ...`

the Cloud Functions runtime service account is missing FCM permissions.

1. Open Google Cloud Console for the same Firebase project.
2. Go to IAM and find the Functions runtime service account.
   - Common accounts are:
     - `<project-id>@appspot.gserviceaccount.com` (1st gen)
     - `<project-number>-compute@developer.gserviceaccount.com` (2nd gen)
3. Grant role: `Firebase Cloud Messaging API Admin`
   (`roles/firebasecloudmessaging.admin`).
4. Ensure the `Firebase Cloud Messaging API` is enabled.
5. Retry sending from the admin panel.
