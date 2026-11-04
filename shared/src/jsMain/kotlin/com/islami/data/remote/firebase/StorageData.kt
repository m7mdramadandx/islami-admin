package com.islami.data.remote.firebase

import dev.gitlive.firebase.storage.Data
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

// A Kotlin/JS ByteArray is an Int8Array, so this views the same buffer without copying.
actual fun ByteArray.toStorageData(): Data {
    val bytes = unsafeCast<Int8Array>()
    return Data(Uint8Array(bytes.buffer, bytes.byteOffset, bytes.length))
}
