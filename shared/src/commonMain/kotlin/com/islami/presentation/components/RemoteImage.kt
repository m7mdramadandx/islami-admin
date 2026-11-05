package com.islami.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.islami.data.remote.RemoteImageLoader
import org.koin.compose.koinInject

private sealed interface ImageState {
    data object Loading : ImageState
    data object Failed : ImageState
    data class Loaded(val image: ImageBitmap) : ImageState
}

/** An image from a URL, with a spinner while loading and a warning icon if it can't be shown. */
@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxWidth: Int = 480,
    contentScale: ContentScale = ContentScale.Crop,
    loader: RemoteImageLoader = koinInject()
) {
    val state by produceState<ImageState>(ImageState.Loading, url, maxWidth) {
        value = loader.load(url, maxWidth)?.let { ImageState.Loaded(it) } ?: ImageState.Failed
    }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        when (val current = state) {
            ImageState.Loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            ImageState.Failed -> Icon(
                Icons.Default.Warning,
                contentDescription = "Image couldn't be loaded",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is ImageState.Loaded -> Image(
                bitmap = current.image,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
