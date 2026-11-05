package com.islami.presentation.components

import androidx.compose.runtime.Composable

// There is no iOS app yet; report "couldn't read" so the UI shows its error instead of doing nothing.
@Composable
actual fun rememberImagePicker(onPicked: (PickedImage?) -> Unit): () -> Unit = { onPicked(null) }
