package com.llego.shared.ui.upload

import androidx.lifecycle.ViewModel
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.upload.ImageUploadService
import com.llego.shared.data.upload.ImageUploadServiceFactory

class ImageUploadViewModel(
    private val service: ImageUploadService = ImageUploadServiceFactory.create(),
    private val tokenManager: TokenManager = TokenManager()
) : ViewModel() {

    suspend fun uploadProductImage(filePath: String): ImageUploadResult {
        val token = tokenManager.getToken()
        return service.uploadProductImage(filePath, token)
    }

    suspend fun uploadUserAvatar(filePath: String): ImageUploadResult {
        val token = tokenManager.getToken()
        return service.uploadUserAvatar(filePath, token)
    }

    suspend fun uploadBusinessAvatar(filePath: String): ImageUploadResult {
        val token = tokenManager.getToken()
        return service.uploadBusinessAvatar(filePath, token)
    }

    suspend fun uploadBranchAvatar(filePath: String): ImageUploadResult {
        val token = tokenManager.getToken()
        return service.uploadBranchAvatar(filePath, token)
    }

    suspend fun uploadBranchCover(filePath: String): ImageUploadResult {
        val token = tokenManager.getToken()
        return service.uploadBranchCover(filePath, token)
    }
}
