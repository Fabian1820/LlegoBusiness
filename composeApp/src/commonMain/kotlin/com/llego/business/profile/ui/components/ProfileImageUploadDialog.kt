package com.llego.business.profile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.ui.components.molecules.ImageUploadPreview
import com.llego.shared.ui.components.molecules.ImageUploadSize

@Composable
fun ImageUploadDialog(
    title: String,
    label: String,
    uploadState: ImageUploadState,
    onStateChange: (ImageUploadState) -> Unit,
    uploadFunction: suspend (filePath: String) -> ImageUploadResult,
    onDismiss: () -> Unit,
    size: ImageUploadSize = ImageUploadSize.MEDIUM,
    previewAspectRatio: Float? = null,
    previewContentScale: ContentScale = ContentScale.Crop,
    helperText: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (!helperText.isNullOrBlank()) {
                    Text(
                        text = helperText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ImageUploadPreview(
                    label = label,
                    uploadState = uploadState,
                    onStateChange = onStateChange,
                    uploadFunction = uploadFunction,
                    size = size,
                    previewAspectRatio = previewAspectRatio,
                    previewContentScale = previewContentScale,
                    showSuccessFileName = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
