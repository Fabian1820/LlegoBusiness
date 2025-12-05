package com.llego.nichos.common.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación Android del selector de imágenes
 */
actual class ImagePickerController(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    internal var currentCallback: ((String) -> Unit)? = null

    actual fun pickImage(onImageSelected: (String) -> Unit) {
        currentCallback = onImageSelected
        // Lanzar el selector de imágenes de Android
        launcher.launch("image/*")
    }
}

/**
 * Crea y recuerda una instancia del ImagePickerController para Android
 */
@Composable
actual fun rememberImagePickerController(): ImagePickerController {
    val controller = remember { mutableListOf<ImagePickerController?>().apply { add(null) } }

    // Launcher para seleccionar imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Convertir URI a String y llamar al callback
            val imageUrl = selectedUri.toString()
            // TODO: En producción, aquí deberías subir la imagen a un servidor
            // y retornar la URL del servidor. Por ahora retornamos la URI local
            controller[0]?.currentCallback?.invoke(imageUrl)
        }
    }

    if (controller[0] == null) {
        controller[0] = ImagePickerController(launcher)
    }

    return controller[0]!!
}
