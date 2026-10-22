package com.islami.data.remote.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize

// Public web client config for the islami-ecc03 project (same values the Flutter web app used).
private val webOptions = FirebaseOptions(
    applicationId = "1:173815807932:web:a1c170fe9f45b5b87935b9",
    apiKey = "AIzaSyDR4C9H78uWJpyhrrMTq_oHckGtHwLsa68",
    projectId = "islami-ecc03",
    storageBucket = "islami-ecc03.appspot.com",
    gcmSenderId = "173815807932",
    authDomain = "islami-ecc03.firebaseapp.com",
    gaTrackingId = "G-CNWNWQKRX9",
)

actual fun initializeFirebase() {
    if (Firebase.apps().isEmpty()) {
        Firebase.initialize(options = webOptions)
    }
}
