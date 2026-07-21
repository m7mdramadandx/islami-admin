package com.islami.data.remote.firebase

class FirebaseCrashlyticsClientImpl : FirebaseCrashlyticsClient {
    override fun recordError(throwable: Throwable, message: String?) {}
    override fun log(message: String) {}
    override fun setUserId(userId: String) {}
    override fun setCustomKey(key: String, value: Any) {}
    override fun setCrashlyticsCollectionEnabled(enabled: Boolean) {}
}
