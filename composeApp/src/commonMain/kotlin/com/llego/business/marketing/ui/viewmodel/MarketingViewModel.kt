package com.llego.business.marketing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.AdCampaign
import com.llego.shared.data.model.AdPricing
import com.llego.shared.data.model.ImageUploadResult
import com.llego.shared.data.model.PaymentMethod
import com.llego.shared.data.repositories.MarketingRepository
import com.llego.shared.data.upload.ImageUploadServiceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketingViewModel(
    private val tokenManager: TokenManager = TokenManager()
) : ViewModel() {
    private val repository = MarketingRepository(tokenManager = tokenManager)
    private val uploadService = ImageUploadServiceFactory.create()

    private val _pricing = MutableStateFlow<List<AdPricing>>(emptyList())
    val pricing: StateFlow<List<AdPricing>> = _pricing.asStateFlow()

    private val _campaigns = MutableStateFlow<List<AdCampaign>>(emptyList())
    val campaigns: StateFlow<List<AdCampaign>> = _campaigns.asStateFlow()

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPricing().onSuccess { _pricing.value = it }
            repository.getPaymentMethods().onSuccess { _paymentMethods.value = it }
            repository.getMyCampaigns()
                .onSuccess { _campaigns.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun durationsFor(placement: String): List<Int> =
        _pricing.value.filter { it.placement == placement }
            .map { it.durationDays }.distinct().sorted()

    fun priceFor(placement: String, durationDays: Int): AdPricing? =
        _pricing.value.firstOrNull { it.placement == placement && it.durationDays == durationDays }

    /**
     * Sube la foto exportada del lienzo y devuelve su S3 path + URL presignada
     * (o null si falla).
     */
    fun uploadCreative(imageBytes: ByteArray, onDone: (imagePath: String?, imageUrl: String?) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            when (val result = uploadService.uploadAdCreative(imageBytes, tokenManager.getToken())) {
                is ImageUploadResult.Success -> {
                    _isUploading.value = false
                    onDone(result.response.imagePath, result.response.imageUrl)
                }
                is ImageUploadResult.Error -> {
                    _error.value = result.message
                    _isUploading.value = false
                    onDone(null, null)
                }
                ImageUploadResult.Loading -> {
                    _isUploading.value = false
                    onDone(null, null)
                }
            }
        }
    }

    fun createAndPurchase(
        businessId: String,
        branchId: String,
        name: String,
        placement: String,
        durationDays: Int,
        creativeImagePath: String,
        paymentMethodId: String,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            repository.createCampaign(businessId, branchId, name, placement, durationDays, creativeImagePath)
                .onSuccess { id ->
                    repository.purchaseCampaign(id, paymentMethodId)
                        .onSuccess {
                            load()
                            _isSubmitting.value = false
                            onDone(true)
                        }
                        .onFailure {
                            _error.value = it.message
                            _isSubmitting.value = false
                            onDone(false)
                        }
                }
                .onFailure {
                    _error.value = it.message
                    _isSubmitting.value = false
                    onDone(false)
                }
        }
    }

    fun clearError() { _error.value = null }
}
