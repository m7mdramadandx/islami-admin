package com.islami.core.serialization

import kotlinx.serialization.json.Json

val jsonConfig = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    isLenient = true
    prettyPrint = false
}
