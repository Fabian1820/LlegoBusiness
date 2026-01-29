package com.llego.business.profile.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    size: ImageUploadSize = ImageUploadSize.MEDIUM
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            ImageUploadPreview(
                label = label,
                uploadState = uploadState,
                onStateChange = onStateChange,
                uploadFunction = uploadFunction,
                size = size,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
