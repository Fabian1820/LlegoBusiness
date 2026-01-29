package com.llego.business.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * Implementación iOS del selector de imágenes
 * Por ahora es un placeholder - la implementación completa requiere configuración adicional
 */
actual class ImagePickerController {
    private val delegate = ImagePickerDelegate()

    actual fun pickImage(onImageSelected: (String) -> Unit) {
        delegate.onImageSelected = onImageSelected
        
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.delegate = delegate
        
        // Find the root view controller safely
        val window = UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
        val rootViewController = window?.rootViewController
        
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

class ImagePickerDelegate : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    var onImageSelected: ((String) -> Unit)? = null

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        
        val imageUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        
        if (imageUrl != null) {
            // Caso 1: Tenemos una URL directa al archivo (mejor opción)
            val urlString = imageUrl.absoluteString ?: ""
            onImageSelected?.invoke(urlString)
        } else if (image != null) {
            // Caso 2: Solo tenemos UIImage, necesitamos guardarlo temporalmente
            val imageData = UIImageJPEGRepresentation(image, 0.8)
            if (imageData != null) {
                val fileManager = NSFileManager.defaultManager
                val tempDir = fileManager.temporaryDirectory
                val fileName = "picked_image_${NSDate().timeIntervalSince1970}.jpg"
                val fileUrl = tempDir.URLByAppendingPathComponent(fileName)
                if (fileUrl != null) {
                    val success = imageData.writeToURL(fileUrl, true)
                    if (success) {
                        val urlString = fileUrl.absoluteString ?: ""
                        onImageSelected?.invoke(urlString)
                    } else {
                        onImageSelected?.invoke("")
                    }
                } else {
                    onImageSelected?.invoke("")
                }
            } else {
                onImageSelected?.invoke("")
            }
        } else {
            onImageSelected?.invoke("")
        }
        
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

@Composable
actual fun rememberImagePickerController(): ImagePickerController {
    return remember { ImagePickerController() }
}
