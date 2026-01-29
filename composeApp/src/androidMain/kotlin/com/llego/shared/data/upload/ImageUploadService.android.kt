package com.llego.shared.data.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
 * Implementación Android del servicio de subida de imágenes usando Ktor Client
 * Soporta tanto paths de archivo como content:// URIs de Android
 */
class AndroidImageUploadService(
    private val context: Context
) : ImageUploadService {

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
            else -> "image/jpeg" // Default a JPEG
        }
    }

    /**
     * Lee los bytes de un archivo, soportando tanto paths como content:// URIs
     */
    private fun readFileBytes(filePath: String): Pair<ByteArray, String>? {
        return try {

            if (filePath.startsWith("content://")) {
                // Es un content URI de Android - usar ContentResolver
                val uri = Uri.parse(filePath)
                val contentResolver = context.contentResolver


                // Obtener el nombre del archivo
                val filename = getFilenameFromUri(uri) ?: "image_${System.currentTimeMillis()}.jpg"

                // Leer los bytes usando ContentResolver
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    return null
                }

                val bytes = inputStream.use { it.readBytes() }
                Pair(bytes, filename)
            } else {
                // Es un path de archivo normal
                val file = File(filePath)
                if (!file.exists()) {
                    return null
                }
                Pair(file.readBytes(), file.name)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Obtiene el nombre del archivo desde un content URI
     */
    private fun getFilenameFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        cursor.getString(nameIndex)
                    } else null
                } else null
            }
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

            val (bytes, filename) = readFileBytes(filePath)
                ?: return ImageUploadResult.Error("No se pudo leer el archivo: $filePath").also {
                }

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
                val errorMsg = "Error ${response.status.value}: ${response.status.description}"
                ImageUploadResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Error al subir imagen: ${e.javaClass.simpleName} - ${e.message}"
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
 * Factory para crear instancias de ImageUploadService en Android
 * Requiere inicialización con Context antes de usar
 */
actual object ImageUploadServiceFactory {
    private var appContext: Context? = null
    
    /**
     * Inicializa el factory con el contexto de la aplicación
     * Debe llamarse una vez al inicio de la app (en Application o MainActivity)
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    
    actual fun create(): ImageUploadService {
        val context = appContext
            ?: throw IllegalStateException("ImageUploadServiceFactory no inicializado. Llama a initialize(context) primero.")
        return AndroidImageUploadService(context)
    }
}
