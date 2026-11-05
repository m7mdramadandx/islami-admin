package com.islami.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.StorageImage
import com.islami.domain.images.storagePathOf

/** Picks or uploads an image in a Storage folder; picks apply immediately, so it stays open. */
@Composable
fun ImageLibraryDialog(
    title: String,
    hint: String,
    /** Null while the folder is still being listed. */
    images: List<StorageImage>?,
    selectedUrl: String,
    /** Width / height of the thumbnails, matching how the app shows these images. */
    aspectRatio: Float,
    isUploading: Boolean,
    onPick: (StorageImage) -> Unit,
    onImagePicked: (PickedImage?) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedPath = storagePathOf(selectedUrl)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    val pickImage = rememberImagePicker(onImagePicked)
                    FilledTonalButton(onClick = pickImage, enabled = !isUploading) {
                        if (isUploading) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Uploading…")
                        } else {
                            Text("Upload new image")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    images == null -> Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { CircularProgressIndicator() }
                    images.isEmpty() -> Text("No images in this folder yet.")
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(if (aspectRatio < 1f) 96.dp else 140.dp),
                        modifier = Modifier.heightIn(max = 480.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images, key = { it.path }) { image ->
                            val selected = image.path == selectedPath
                            Box(
                                Modifier
                                    .aspectRatio(aspectRatio)
                                    .clip(MaterialTheme.shapes.small)
                                    .border(
                                        if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                                        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        MaterialTheme.shapes.small
                                    )
                                    .clickable { onPick(image) }
                            ) {
                                RemoteImage(url = image.url, contentDescription = image.path, maxWidth = 320, modifier = Modifier.matchParentSize())
                                if (selected) {
                                    Icon(
                                        Icons.Default.Check, contentDescription = "Current",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                            .clip(CircleShape).background(MaterialTheme.colorScheme.primary).padding(2.dp).size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
