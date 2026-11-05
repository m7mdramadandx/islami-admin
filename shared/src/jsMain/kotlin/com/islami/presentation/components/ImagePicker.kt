package com.islami.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
actual fun rememberImagePicker(onPicked: (PickedImage?) -> Unit): () -> Unit {
    val callback by rememberUpdatedState(onPicked)
    return remember {
        {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/jpeg,image/png,image/webp"
            input.onchange = {
                val file = input.files?.get(0)
                if (file == null) {
                    callback(null)
                } else {
                    val reader = FileReader()
                    reader.onload = {
                        // A Kotlin/JS ByteArray is an Int8Array, so this wraps the buffer without copying.
                        val bytes = Int8Array(reader.result as ArrayBuffer).unsafeCast<ByteArray>()
                        callback(PickedImage(file.name, bytes))
                    }
                    reader.onerror = { callback(null) }
                    reader.readAsArrayBuffer(file)
                }
            }
            input.click()
        }
    }
}
