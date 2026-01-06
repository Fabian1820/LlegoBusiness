package com.llego.shared.data.upload

import com.llego.shared.data.model.ImageUploadResponse
import com.llego.shared.data.model.ImageUploadResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cinterop.*
import kotlinx.serialization.json.Json
import platform.Foundation.*

/**
 * Implementación iOS del servicio de subida de imágenes usando Ktor Client
 */
class IosImageUploadService : ImageUploadService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
    }

    private val baseUrl = "http://localhost:4000"

    /**
     * Convierte NSData a ByteArray de forma segura
     */
    @OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
    private fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        val bytes = ByteArray(length)

        if (length > 0) {
            bytes.usePinned { pinnedBytes ->
                platform.posix.memcpy(pinnedBytes.addressOf(0), this.bytes, this.length)
            }
        }

        return bytes
    }

    /**
     * Método genérico para subir imágenes a cualquier endpoint
     */
    private suspend fun uploadImage(
        endpoint: String,
        filePath: String,
        token: String?
    ): ImageUploadResult {
        return try {
            val fileUrl = NSURL.fileURLWithPath(filePath)
            val fileData = NSData.dataWithContentsOfURL(fileUrl)

            if (fileData == null) {
                return ImageUploadResult.Error("El archivo no existe: $filePath")
            }

            // Convertir NSData a ByteArray
            val bytes = fileData.toByteArray()
            val fileName = fileUrl.lastPathComponent ?: "image.jpg"

            val response = client.submitFormWithBinaryData(
                url = "$baseUrl$endpoint",
                formData = formData {
                    append("image", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/*")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ) {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }

            if (response.status.isSuccess()) {
                val uploadResponse: ImageUploadResponse = response.body()
                ImageUploadResult.Success(uploadResponse)
            } else {
                ImageUploadResult.Error("Error ${response.status.value}: ${response.status.description}")
            }
        } catch (e: Exception) {
            ImageUploadResult.Error("Error al subir imagen: ${e.message}")
        }
    }

    override suspend fun uploadProductImage(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/product/image", filePath, token)
    }

    override suspend fun uploadUserAvatar(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/user/avatar", filePath, token)
    }

    override suspend fun uploadBusinessAvatar(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/business/avatar", filePath, token)
    }

    override suspend fun uploadBusinessCover(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/business/cover", filePath, token)
    }

    override suspend fun uploadBranchAvatar(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/branch/avatar", filePath, token)
    }

    override suspend fun uploadBranchCover(filePath: String, token: String?): ImageUploadResult {
        return uploadImage("/upload/branch/cover", filePath, token)
    }
}

/**
 * Factory para crear instancias de ImageUploadService en iOS
 */
actual object ImageUploadServiceFactory {
    actual fun create(): ImageUploadService = IosImageUploadService()
}
