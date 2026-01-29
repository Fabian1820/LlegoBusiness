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
            requestTimeoutMillis = 60000  // 60 segundos para uploads
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }
    }

    // Backend URL desde Railway
    private val baseUrl = BackendConfig.REST_URL

    /**
     * Detecta el tipo MIME basado en la extensión del archivo
     */
    private fun getMimeType(filename: String): String {
        return when (filename.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "heic" -> "image/heic"
            else -> "image/jpeg" // Default a JPEG
        }
    }

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
     * Lee los bytes de un archivo, soportando tanto paths como file:// URLs
     */
    private fun readFileBytes(filePath: String): Pair<ByteArray, String>? {
        return try {

            // Crear NSURL desde el path (maneja tanto file:// URLs como paths absolutos)
            val fileUrl = if (filePath.startsWith("file://")) {
                NSURL(string = filePath)
            } else {
                NSURL.fileURLWithPath(filePath)
            }

            if (fileUrl == null) {
                return null
            }


            // Leer los datos del archivo
            val fileData = NSData.dataWithContentsOfURL(fileUrl)
            if (fileData == null) {
                return null
            }

            // Convertir NSData a ByteArray
            val bytes = fileData.toByteArray()

            // Validar que los bytes no estén vacíos
            if (bytes.isEmpty()) {
                return null
            }

            // Obtener el nombre del archivo
            val filename = fileUrl.lastPathComponent ?: "image_${NSDate().timeIntervalSince1970}.jpg"

            Pair(bytes, filename)
        } catch (e: Exception) {
            null
        }
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

            val readResult = readFileBytes(filePath)
            if (readResult == null) {
                val errorMsg = "No se pudo acceder a la imagen. Verifica los permisos de la app."
                return ImageUploadResult.Error(errorMsg)
            }

            val (bytes, filename) = readResult
            val mimeType = getMimeType(filename)

            // Usar submitFormWithBinaryData - Ktor maneja el Content-Type multipart/form-data automáticamente
            val response = client.submitFormWithBinaryData(
                url = "$baseUrl$endpoint",
                formData = formData {
                    // FastAPI espera: name="image", filename presente, Content-Type del archivo
                    append("image", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    })
                }
            ) {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
                // NO establecer Content-Type manualmente - Ktor lo genera con boundary
            }


            if (response.status.isSuccess()) {
                val uploadResponse: ImageUploadResponse = response.body()
                ImageUploadResult.Success(uploadResponse)
            } else {
                val errorMsg = "Error del servidor (${response.status.value}): ${response.status.description}"
                ImageUploadResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Error al subir imagen: ${e.message ?: "Error desconocido"}"
            ImageUploadResult.Error(errorMsg)
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
