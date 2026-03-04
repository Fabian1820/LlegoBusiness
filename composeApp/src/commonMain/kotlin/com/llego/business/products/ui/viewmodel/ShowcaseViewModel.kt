package com.llego.business.products.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.ShowcaseDetectionResult
import com.llego.shared.data.model.ShowcaseItem
import com.llego.shared.data.model.ShowcasesResult
import com.llego.shared.data.repositories.ShowcaseRepository
import com.llego.shared.data.upload.ImageUploadService
import com.llego.shared.data.upload.ImageUploadServiceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShowcaseViewModel(
    private val tokenManager: TokenManager,
    private val uploadService: ImageUploadService = ImageUploadServiceFactory.create()
) : ViewModel() {

    private val repository = ShowcaseRepository(tokenManager)

    private val _showcasesState = MutableStateFlow<ShowcasesResult>(ShowcasesResult.Loading)
    val showcasesState: StateFlow<ShowcasesResult> = _showcasesState.asStateFlow()

    private val _detectionState = MutableStateFlow<ShowcaseDetectionResult>(ShowcaseDetectionResult.Idle)
    val detectionState: StateFlow<ShowcaseDetectionResult> = _detectionState.asStateFlow()

    fun loadShowcases(branchId: String, activeOnly: Boolean = false) {
        viewModelScope.launch {
            _showcasesState.value = ShowcasesResult.Loading
            _showcasesState.value = repository.getShowcasesByBranch(
                branchId = branchId,
                activeOnly = activeOnly
            )
        }
    }

    suspend fun createShowcase(
        branchId: String,
        title: String,
        imagePath: String,
        description: String? = null,
        items: List<ShowcaseItem>? = null
    ): ShowcasesResult {
        _showcasesState.value = ShowcasesResult.Loading
        val result = repository.createShowcase(
            branchId = branchId,
            title = title,
            imagePath = imagePath,
            description = description,
            items = items
        )
        _showcasesState.value = result
        return result
    }

    suspend fun toggleAvailability(showcaseId: String, isActive: Boolean): ShowcasesResult {
        _showcasesState.value = ShowcasesResult.Loading
        val result = repository.toggleAvailability(showcaseId, isActive)
        _showcasesState.value = result
        return result
    }

    suspend fun detectProductsFromShowcase(filePath: String): ShowcaseDetectionResult {
        _detectionState.value = ShowcaseDetectionResult.Loading
        val token = tokenManager.getToken()
        val result = uploadService.detectProductsFromShowcase(filePath, token)
        _detectionState.value = result
        return result
    }
}
