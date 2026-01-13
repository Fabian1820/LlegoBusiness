package com.llego.shared.data.upload

import com.llego.shared.data.model.ImageUploadResult

/**
 * Servicio multiplataforma para subir imágenes a diferentes endpoints REST
 *
 * Cada plataforma debe implementar la lógica de selección de archivo y subida:
 * - Android: Intent de selección de archivo + OkHttp/Ktor
 * - iOS: UIImagePickerController + URLSession
 * - JVM: JFileChooser + Ktor Client
 */
interface ImageUploadService {

    /**
     * Sube una imagen de producto
     * @param filePath Path del archivo a subir (específico de plataforma)
     * @param token JWT token para autenticación
     * @return ImageUploadResult con la respuesta del servidor
     */
    suspend fun uploadProductImage(
        filePath: String,
        token: String?
    ): ImageUploadResult

    /**
     * Sube un avatar de usuario
     * @param filePath Path del archivo a subir
     * @param token JWT token para autenticación
     * @return ImageUploadResult con la respuesta del servidor
     */
    suspend fun uploadUserAvatar(
        filePath: String,
        token: String?
    ): ImageUploadResult

    /**
     * Sube un avatar de negocio
     * @param filePath Path del archivo a subir
     * @param token JWT token para autenticación
     * @return ImageUploadResult con la respuesta del servidor
     */
    suspend fun uploadBusinessAvatar(
        filePath: String,
        token: String?
    ): ImageUploadResult

    /**
     * Sube un avatar de sucursal
     * @param filePath Path del archivo a subir
     * @param token JWT token para autenticación
     * @return ImageUploadResult con la respuesta del servidor
     */
    suspend fun uploadBranchAvatar(
        filePath: String,
        token: String?
    ): ImageUploadResult

    /**
     * Sube una portada de sucursal
     * @param filePath Path del archivo a subir
     * @param token JWT token para autenticación
     * @return ImageUploadResult con la respuesta del servidor
     */
    suspend fun uploadBranchCover(
        filePath: String,
        token: String?
    ): ImageUploadResult
}

/**
 * Factory object para crear instancias de ImageUploadService
 * Cada plataforma implementa la creación de la instancia específica
 */
expect object ImageUploadServiceFactory {
    fun create(): ImageUploadService
}
