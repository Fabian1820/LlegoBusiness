package com.llego.nichos.common.ui.components

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
        println("🎯 ImagePickerController: Launching image picker")
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
            println("❌ ImagePickerController: No URI selected")
            return@rememberLauncherForActivityResult
        }

        println("✅ ImagePickerController: URI selected: $uri")

        try {
            // IMPORTANTE: Tomar permisos persistentes sobre la URI
            // Esto permite que la URI siga siendo válida incluso después de que el selector de archivos se cierre
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            println("🔓 ImagePickerController: Persistable URI permissions granted")
        } catch (e: SecurityException) {
            // Algunos URIs no soportan permisos persistentes (ej: Google Photos)
            // En ese caso, seguimos adelante con permisos temporales
            println("⚠️ ImagePickerController: Could not take persistable permissions (${e.message}), using temporary permissions")
        } catch (e: Exception) {
            println("⚠️ ImagePickerController: Unexpected error taking permissions: ${e.message}")
        }

        // Verificar que podemos acceder a la URI
        try {
            val testStream = context.contentResolver.openInputStream(uri)
            if (testStream == null) {
                println("❌ ImagePickerController: Failed to open test stream - URI may be invalid")
                return@rememberLauncherForActivityResult
            }
            testStream.close()
            println("✅ ImagePickerController: URI is readable")
        } catch (e: Exception) {
            println("❌ ImagePickerController: Error testing URI access: ${e.javaClass.simpleName} - ${e.message}")
            return@rememberLauncherForActivityResult
        }

        // Convertir URI a String y llamar al callback
        val imageUrl = uri.toString()
        println("📞 ImagePickerController: Invoking callback with URI: $imageUrl")
        controller[0]?.currentCallback?.invoke(imageUrl)
    }

    if (controller[0] == null) {
        val pickerController = ImagePickerController(launcher)
        pickerController.context = context
        controller[0] = pickerController
    }

    return controller[0]!!
}
