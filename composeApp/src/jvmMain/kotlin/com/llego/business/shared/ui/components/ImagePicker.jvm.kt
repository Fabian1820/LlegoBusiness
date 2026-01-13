package com.llego.business.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File

/**
 * Implementación Desktop/JVM del selector de imágenes usando JFileChooser
 */
actual class ImagePickerController {
    actual fun pickImage(onImageSelected: (String) -> Unit) {
        // Crear file chooser
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Seleccionar imagen del producto"
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

        // Filtro para solo mostrar imágenes
        val filter = FileNameExtensionFilter(
            "Imágenes (*.jpg, *.jpeg, *.png, *.gif)",
            "jpg", "jpeg", "png", "gif"
        )
        fileChooser.fileFilter = filter

        // Mostrar diálogo
        val result = fileChooser.showOpenDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile: File = fileChooser.selectedFile
            // Retornar la ruta del archivo
            onImageSelected("file://${selectedFile.absolutePath}")
        }
    }
}

/**
 * Crea y recuerda una instancia del ImagePickerController para Desktop
 */
@Composable
actual fun rememberImagePickerController(): ImagePickerController {
    return remember {
        ImagePickerController()
    }
}
