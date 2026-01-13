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
            println("🔍 IosImageUploadService: Reading file from path: $filePath")

            // Crear NSURL desde el path (maneja tanto file:// URLs como paths absolutos)
            val fileUrl = if (filePath.startsWith("file://")) {
                println("📱 IosImageUploadService: Processing file:// URL")
                NSURL(string = filePath)
            } else {
                println("💾 IosImageUploadService: Processing file path")
                NSURL.fileURLWithPath(filePath)
            }

            if (fileUrl == null) {
                println("❌ IosImageUploadService: Failed to create NSURL from path")
                return null
            }

            println("📂 IosImageUploadService: Reading file from URL: ${fileUrl.absoluteString}")

            // Leer los datos del archivo
            val fileData = NSData.dataWithContentsOfURL(fileUrl)
            if (fileData == null) {
                println("❌ IosImageUploadService: Failed to read file data - file may not exist or permission denied")
                return null
            }

            // Convertir NSData a ByteArray
            val bytes = fileData.toByteArray()
            println("✅ IosImageUploadService: Successfully read ${bytes.size} bytes (${bytes.size / 1024} KB)")

            // Validar que los bytes no estén vacíos
            if (bytes.isEmpty()) {
                println("❌ IosImageUploadService: Read 0 bytes from file")
                return null
            }

            // Obtener el nombre del archivo
            val filename = fileUrl.lastPathComponent ?: "image_${NSDate().timeIntervalSince1970}.jpg"
            println("📝 IosImageUploadService: Filename extracted: $filename")

            Pair(bytes, filename)
        } catch (e: Exception) {
            println("❌ IosImageUploadService: EXCEPTION reading file - ${e.message}")
            e.printStackTrace()
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
            println("🚀 IosImageUploadService: Starting upload to $endpoint")
            println("📍 IosImageUploadService: File path: $filePath")
            println("🔑 IosImageUploadService: Token present: ${token != null}")

            val readResult = readFileBytes(filePath)
            if (readResult == null) {
                val errorMsg = "No se pudo acceder a la imagen. Verifica los permisos de la app."
                println("❌ IosImageUploadService: Failed to read file bytes")
                return ImageUploadResult.Error(errorMsg)
            }

            val (bytes, filename) = readResult
            val mimeType = getMimeType(filename)
            println("📤 IosImageUploadService: Uploading $filename (${bytes.size} bytes / ${bytes.size / 1024} KB, $mimeType) to $baseUrl$endpoint")

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

            println("📡 IosImageUploadService: Response status: ${response.status.value} ${response.status.description}")

            if (response.status.isSuccess()) {
                val uploadResponse: ImageUploadResponse = response.body()
                println("✅ IosImageUploadService: Upload success - ${uploadResponse.imagePath}")
                ImageUploadResult.Success(uploadResponse)
            } else {
                val errorMsg = "Error del servidor (${response.status.value}): ${response.status.description}"
                println("❌ IosImageUploadService: Upload failed - $errorMsg")
                ImageUploadResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Error al subir imagen: ${e.message ?: "Error desconocido"}"
            println("❌ IosImageUploadService: EXCEPTION during upload - ${e.message}")
            e.printStackTrace()
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
