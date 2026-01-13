package com.llego.shared.ui.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.llego.business.shared.ui.components.rememberImagePickerController
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.data.model.extractFilename
import kotlinx.coroutines.launch

enum class ImageUploadSize(val height: Dp) {
    SMALL(80.dp),
    MEDIUM(120.dp),
    LARGE(200.dp)
}

@Composable
fun ImageUploadPreview(
    label: String,
    uploadState: ImageUploadState,
    onStateChange: (ImageUploadState) -> Unit,
    uploadFunction: suspend (filePath: String, token: String?) -> ImageUploadResult,
    size: ImageUploadSize = ImageUploadSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val imagePickerController = rememberImagePickerController()
    val tokenManager = remember { TokenManager() }

    Card(
        modifier = modifier.height(size.height + 40.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(size.height)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = uploadState is ImageUploadState.Idle || 
                                        uploadState is ImageUploadState.Success ||
                                        uploadState is ImageUploadState.Error) {
                        imagePickerController.pickImage { selectedUri ->
                            val filename = selectedUri.extractFilename()
                            println("ImageUploadPreview: Image selected - $filename")
                            onStateChange(ImageUploadState.Selected(selectedUri, filename))
                            
                            scope.launch {
                                println("ImageUploadPreview: Starting upload for $filename")
                                onStateChange(ImageUploadState.Uploading(selectedUri, filename))
                                
                                try {
                                    val token = tokenManager.getToken()
                                    when (val result = uploadFunction(selectedUri, token)) {
                                        is ImageUploadResult.Success -> {
                                            println("ImageUploadPreview: Upload success - ${result.response.imagePath}")
                                            onStateChange(ImageUploadState.Success(selectedUri, result.response.imagePath, filename))
                                        }
                                        is ImageUploadResult.Error -> {
                                            println("ImageUploadPreview: Upload error - ${result.message}")
                                            onStateChange(ImageUploadState.Error(selectedUri, result.message, filename))
                                        }
                                        is ImageUploadResult.Loading -> {}
                                    }
                                } catch (e: Exception) {
                                    println("ImageUploadPreview: Upload exception - ${e.message}")
                                    onStateChange(ImageUploadState.Error(selectedUri, e.message ?: "Error desconocido", filename))
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when (uploadState) {
                    is ImageUploadState.Idle -> IdleContent(label, size)
                    is ImageUploadState.Selected -> LoadingContent(uploadState.localUri, label)
                    is ImageUploadState.Uploading -> LoadingContent(uploadState.localUri, label)
                    is ImageUploadState.Success -> SuccessContent(uploadState.localUri, label)
                    is ImageUploadState.Error -> ErrorContent(uploadState.localUri, label) {
                        // Retry logic
                        val uri = uploadState.localUri
                        val filename = uploadState.filename
                        scope.launch {
                            onStateChange(ImageUploadState.Uploading(uri, filename))
                            try {
                                val token = tokenManager.getToken()
                                when (val result = uploadFunction(uri, token)) {
                                    is ImageUploadResult.Success -> {
                                        onStateChange(ImageUploadState.Success(uri, result.response.imagePath, filename))
                                    }
                                    is ImageUploadResult.Error -> {
                                        onStateChange(ImageUploadState.Error(uri, result.message, filename))
                                    }
                                    is ImageUploadResult.Loading -> {}
                                }
                            } catch (e: Exception) {
                                onStateChange(ImageUploadState.Error(uri, e.message ?: "Error desconocido", filename))
                            }
                        }
                    }
                }
            }

            // Status text area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (uploadState) {
                    is ImageUploadState.Idle -> Text(
                        text = "Toca para agregar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is ImageUploadState.Selected, is ImageUploadState.Uploading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                        Text("Subiendo...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    is ImageUploadState.Success -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp), tint = Color(0xFF4CAF50))
                        Text(uploadState.filename, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    is ImageUploadState.Error -> Text(
                        text = "Error - Toca para reintentar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(label: String, size: ImageUploadSize) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.Default.AddPhotoAlternate,
            contentDescription = "Agregar $label",
            modifier = Modifier.size(if (size == ImageUploadSize.LARGE) 48.dp else 32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = if (size == ImageUploadSize.LARGE) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingContent(localUri: String, label: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = localUri,
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun SuccessContent(localUri: String, label: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = localUri,
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            shape = CircleShape,
            color = Color(0xFF4CAF50),
            shadowElevation = 4.dp
        ) {
            Icon(Icons.Default.Check, "Subida exitosa", Modifier.size(24.dp).padding(4.dp), tint = Color.White)
        }
    }
}

@Composable
private fun ErrorContent(localUri: String, label: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().clickable { onRetry() }) {
        AsyncImage(
            model = localUri,
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Error, "Error", Modifier.size(32.dp), tint = Color.White)
                Text("Toca para reintentar", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}
