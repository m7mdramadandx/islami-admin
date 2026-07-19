package com.islami.data.remote.firebase

import android.content.Context
import com.google.firebase.FirebaseApp

actual fun initializeFirebase() {
    // For Android, Firebase is typically initialized via the google-services plugin 
    // and FirebaseContentProvider automatically. 
    // However, we can provide this if manual init is needed or for other platforms.
}

// In Android, we might want to provide a way to init with context if needed
fun initializeFirebase(context: Context) {
    FirebaseApp.initializeApp(context)
}
