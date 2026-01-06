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
