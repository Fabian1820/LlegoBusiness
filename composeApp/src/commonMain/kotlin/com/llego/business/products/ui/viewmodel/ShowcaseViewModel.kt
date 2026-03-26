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

    private data class ShowcasesQuery(
        val branchId: String,
        val activeOnly: Boolean
    )

    private val repository = ShowcaseRepository(tokenManager)

    private val _showcasesState = MutableStateFlow<ShowcasesResult>(ShowcasesResult.Loading)
    val showcasesState: StateFlow<ShowcasesResult> = _showcasesState.asStateFlow()
    private var lastLoadedShowcasesQuery: ShowcasesQuery? = null

    private val _detectionState = MutableStateFlow<ShowcaseDetectionResult>(ShowcaseDetectionResult.Idle)
    val detectionState: StateFlow<ShowcaseDetectionResult> = _detectionState.asStateFlow()

    fun loadShowcases(branchId: String, activeOnly: Boolean = false, force: Boolean = false) {
        val previousQuery = lastLoadedShowcasesQuery
        val query = ShowcasesQuery(branchId = branchId, activeOnly = activeOnly)
        if (!force &&
            query == lastLoadedShowcasesQuery &&
            _showcasesState.value is ShowcasesResult.Success
        ) {
            return
        }

        viewModelScope.launch {
            val queryChanged = previousQuery != query
            if (_showcasesState.value !is ShowcasesResult.Success || queryChanged) {
                _showcasesState.value = ShowcasesResult.Loading
            }

            val result = repository.getShowcasesByBranch(
                branchId = branchId,
                activeOnly = activeOnly
            )
            _showcasesState.value = result
            if (result is ShowcasesResult.Success) {
                lastLoadedShowcasesQuery = query
            }
        }
    }

    fun ensureShowcasesLoaded(branchId: String, activeOnly: Boolean = false) {
        loadShowcases(
            branchId = branchId,
            activeOnly = activeOnly,
            force = false
        )
    }

    fun invalidateShowcasesCache() {
        lastLoadedShowcasesQuery = null
        _showcasesState.value = ShowcasesResult.Loading
    }

    suspend fun createShowcase(
        branchId: String,
        title: String,
        imagePath: String,
        description: String? = null,
        items: List<ShowcaseItem>? = null
    ): ShowcasesResult {
        invalidateShowcasesCache()
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

    suspend fun updateShowcase(
        showcaseId: String,
        title: String? = null,
        imagePath: String? = null,
        description: String? = null,
        items: List<ShowcaseItem>? = null,
        isActive: Boolean? = null
    ): ShowcasesResult {
        val previousState = _showcasesState.value as? ShowcasesResult.Success
        _showcasesState.value = ShowcasesResult.Loading
        val result = repository.updateShowcase(
            showcaseId = showcaseId,
            title = title,
            imagePath = imagePath,
            description = description,
            items = items,
            isActive = isActive
        )
        if (result is ShowcasesResult.Success) {
            val updated = result.showcases.firstOrNull()
            val previousShowcases = previousState?.showcases
            _showcasesState.value = if (updated != null && previousShowcases != null) {
                ShowcasesResult.Success(
                    previousShowcases.map { showcase ->
                        if (showcase.id == updated.id) updated else showcase
                    }
                )
            } else {
                result
            }
        } else {
            _showcasesState.value = result
        }
        return result
    }

    suspend fun toggleAvailability(showcaseId: String, isActive: Boolean): ShowcasesResult {
        val previousState = _showcasesState.value as? ShowcasesResult.Success
        val previousShowcases = previousState?.showcases

        if (previousShowcases == null) {
            val result = repository.toggleAvailability(showcaseId, isActive)
            if (result is ShowcasesResult.Success) {
                invalidateShowcasesCache()
            }
            return result
        }

        val optimisticShowcases = previousShowcases.map { showcase ->
            if (showcase.id == showcaseId) showcase.copy(isActive = isActive) else showcase
        }
        _showcasesState.value = ShowcasesResult.Success(optimisticShowcases)

        return when (val result = repository.toggleAvailability(showcaseId, isActive)) {
            is ShowcasesResult.Success -> {
                _showcasesState.value = ShowcasesResult.Success(optimisticShowcases)
                result
            }

            is ShowcasesResult.Error -> {
                _showcasesState.value = ShowcasesResult.Success(previousShowcases)
                result
            }

            is ShowcasesResult.Loading -> {
                _showcasesState.value = ShowcasesResult.Success(previousShowcases)
                ShowcasesResult.Error("No se pudo actualizar disponibilidad")
            }
        }
    }

    suspend fun deleteShowcase(showcaseId: String): ShowcasesResult {
        val previousState = _showcasesState.value as? ShowcasesResult.Success
        val previousShowcases = previousState?.showcases

        if (previousShowcases == null) {
            val result = repository.deleteShowcase(showcaseId)
            if (result is ShowcasesResult.Success) {
                invalidateShowcasesCache()
            }
            return result
        }

        val optimisticShowcases = previousShowcases.filterNot { it.id == showcaseId }
        _showcasesState.value = ShowcasesResult.Success(optimisticShowcases)

        return when (val result = repository.deleteShowcase(showcaseId)) {
            is ShowcasesResult.Success -> {
                _showcasesState.value = ShowcasesResult.Success(optimisticShowcases)
                result
            }

            is ShowcasesResult.Error -> {
                _showcasesState.value = ShowcasesResult.Success(previousShowcases)
                result
            }

            is ShowcasesResult.Loading -> {
                _showcasesState.value = ShowcasesResult.Success(previousShowcases)
                ShowcasesResult.Error("No se pudo eliminar la vitrina")
            }
        }
    }

    suspend fun detectProductsFromShowcase(filePath: String): ShowcaseDetectionResult {
        _detectionState.value = ShowcaseDetectionResult.Loading
        val token = tokenManager.getToken()
        val result = uploadService.detectProductsFromShowcase(filePath, token)
        _detectionState.value = result
        return result
    }
}
