
# Islami Admin

This is the admin panel for the Islami app. It is a Flutter application that allows admins to manage the content of the app.

## Features

- **Quran Management:** Manage the Quranic text and translations.
- **Hadith Management:** Manage the hadith collections.
- **User Management:** Manage users and their roles.
- **Notification Management:** Send notifications to users.
- **Azkar Management:** Manage the azkar (remembrances).
- **Duas Management:** Manage the duas (supplications).

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://docs.flutter.dev/cookbook)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

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

## Dashboard analytics setup (GA4 + BigQuery)

To populate dashboard event metrics (DAU/WAU/MAU and event counts), configure:

1. Enable Firebase Analytics for your mobile app.
2. Enable BigQuery export for Analytics in Firebase Console.
3. Create Firestore document `config/analytics` with fields:
   - `bigQueryDatasetId` (example: `analytics_173815807932`)
   - `bigQueryLocation` (example: `US`)
4. Create Firestore document `config/app` with:
   - `version` (example: `1.2.3`)
5. Deploy Cloud Functions so `getAnalyticsStats` is available.
