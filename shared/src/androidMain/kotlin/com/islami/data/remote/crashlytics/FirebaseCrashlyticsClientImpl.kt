package com.islami.data.remote.crashlytics

import android.util.Log
import com.islami.data.remote.firebase.FirebaseCrashlyticsClient
import com.google.firebase.crashlytics.FirebaseCrashlytics

actual class FirebaseCrashlyticsClientImpl : FirebaseCrashlyticsClient {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun recordError(throwable: Throwable, message: String?) {
        Log.e("Crashlytics", message ?: "An error occurred", throwable)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        Log.d("Crashlytics", message)
        crashlytics.log(message)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value.toLong())
    }

    override fun setCustomKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }
}
