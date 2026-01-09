package com.llego.shared.data.upload

import com.llego.shared.data.model.ImageUploadResponse
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.network.BackendConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Implementación JVM/Desktop del servicio de subida de imágenes usando Ktor Client
 */
class JvmImageUploadService : ImageUploadService {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000  // 60 segundos para uploads
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }
    }

    // Backend URL desde Railway
    private val baseUrl = BackendConfig.REST_URL

    /**
     * Método genérico para subir imágenes a cualquier endpoint
     */
    private suspend fun uploadImage(
        endpoint: String,
        filePath: String,
        token: String?
    ): ImageUploadResult {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return ImageUploadResult.Error("El archivo no existe: $filePath")
            }

            val response = client.submitFormWithBinaryData(
                url = "$baseUrl$endpoint",
                formData = formData {
                    append("image", file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/*")
                        append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
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
 * Factory para crear instancias de ImageUploadService en JVM/Desktop
 */
actual object ImageUploadServiceFactory {
    actual fun create(): ImageUploadService = JvmImageUploadService()
}
