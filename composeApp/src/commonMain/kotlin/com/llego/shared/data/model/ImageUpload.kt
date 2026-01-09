package com.llego.shared.data.model

import kotlinx.serialization.Serializable

/**
 * Resultado de la subida de una imagen
 */
@Serializable
data class ImageUploadResponse(
    val imagePath: String,
    val imageUrl: String
)

/**
 * Resultado de operaciones de subida de imágenes
 */
sealed class ImageUploadResult {
    data class Success(val response: ImageUploadResponse) : ImageUploadResult()
    data class Error(val message: String) : ImageUploadResult()
    data object Loading : ImageUploadResult()
}

/**
 * Tipos de entidades para las que se pueden subir imágenes
 */
enum class ImageUploadEntityType {
    PRODUCT,        // Imagen de producto
    USER_AVATAR,    // Avatar de usuario
    BUSINESS_AVATAR, // Avatar de negocio
    BUSINESS_COVER,  // Portada de negocio
    BRANCH_AVATAR,   // Avatar de sucursal
    BRANCH_COVER     // Portada de sucursal
}

/**
 * Estado del proceso de selección y subida de imagen.
 * Representa el ciclo completo: Idle → Selected → Uploading → (Success | Error)
 */
sealed class ImageUploadState {
    /** Estado inicial, sin imagen seleccionada */
    data object Idle : ImageUploadState()
    
    /** Imagen seleccionada, lista para subir */
    data class Selected(
        val localUri: String,
        val filename: String
    ) : ImageUploadState()
    
    /** Imagen en proceso de subida a S3 */
    data class Uploading(
        val localUri: String,
        val filename: String,
        val progress: Float? = null
    ) : ImageUploadState()
    
    /** Subida completada exitosamente */
    data class Success(
        val localUri: String,
        val s3Path: String,
        val filename: String
    ) : ImageUploadState()
    
    /** Error durante la subida */
    data class Error(
        val localUri: String,
        val message: String,
        val filename: String
    ) : ImageUploadState()
}

/**
 * Extrae el nombre del archivo de una URI o path.
 * Funciona con URIs de Android (content://), paths de archivo, y URLs.
 */
fun String.extractFilename(): String {
    return this
        .substringAfterLast("/")
        .substringAfterLast("\\")
        .substringBefore("?") // Remover query params si existen
        .ifEmpty { "imagen" }
}
