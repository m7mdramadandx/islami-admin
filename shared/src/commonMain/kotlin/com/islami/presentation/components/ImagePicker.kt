package com.islami.presentation.components

import androidx.compose.runtime.Composable

class PickedImage(val name: String, val bytes: ByteArray)

/**
 * Returns a function that opens the platform's image picker. [onPicked] gets the chosen file, or
 * null if it couldn't be read. Cancelling the picker may not call it at all (browsers don't report it).
 */
@Composable
expect fun rememberImagePicker(onPicked: (PickedImage?) -> Unit): () -> Unit
