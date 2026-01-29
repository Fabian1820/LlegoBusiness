package com.llego.business.shared.ui.components

import android.content.Context
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
    internal var context: Context? = null

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
    val context = LocalContext.current
    val controller = remember { mutableListOf<ImagePickerController?>().apply { add(null) } }

    // Launcher para seleccionar imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }


        try {
            // IMPORTANTE: Tomar permisos persistentes sobre la URI
            // Esto permite que la URI siga siendo válida incluso después de que el selector de archivos se cierre
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: SecurityException) {
            // Algunos URIs no soportan permisos persistentes (ej: Google Photos)
            // En ese caso, seguimos adelante con permisos temporales
        } catch (e: Exception) {
        }

        // Verificar que podemos acceder a la URI
        try {
            val testStream = context.contentResolver.openInputStream(uri)
            if (testStream == null) {
                return@rememberLauncherForActivityResult
            }
            testStream.close()
        } catch (e: Exception) {
            return@rememberLauncherForActivityResult
        }

        // Convertir URI a String y llamar al callback
        val imageUrl = uri.toString()
        controller[0]?.currentCallback?.invoke(imageUrl)
    }

    if (controller[0] == null) {
        val pickerController = ImagePickerController(launcher)
        pickerController.context = context
        controller[0] = pickerController
    }

    return controller[0]!!
}
