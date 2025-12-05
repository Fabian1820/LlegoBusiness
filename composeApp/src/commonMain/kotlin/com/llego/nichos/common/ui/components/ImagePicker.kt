package com.llego.nichos.common.ui.components

import androidx.compose.runtime.Composable

/**
 * Interfaz multiplataforma para seleccionar imágenes
 * Cada plataforma implementará su propia lógica (Android: Intent, iOS: UIImagePicker)
 */
expect class ImagePickerController {
    /**
     * Lanza el selector de imágenes nativo de la plataforma
     * @param onImageSelected Callback que retorna la URI/Path de la imagen seleccionada
     */
    fun pickImage(onImageSelected: (String) -> Unit)
}

/**
 * Composable multiplataforma para obtener una instancia del ImagePickerController
 */
@Composable
expect fun rememberImagePickerController(): ImagePickerController
