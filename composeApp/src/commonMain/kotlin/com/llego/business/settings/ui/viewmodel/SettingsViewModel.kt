package com.llego.business.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.settings.data.model.BusinessSettings
import com.llego.business.settings.data.model.isCurrentlyOpen
import com.llego.business.settings.data.repository.SettingsRepository
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para gestion de configuracion del restaurante.
 */
class SettingsViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository = SettingsRepository.getInstance(tokenManager)

    // Estado de UI
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Settings actuales
    private val _settings = MutableStateFlow<BusinessSettings?>(null)
    val settings: StateFlow<BusinessSettings?> = _settings.asStateFlow()

    init {
        observeSettings()
        loadSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.settings.collect { currentSettings ->
                _settings.value = currentSettings
                _uiState.value = SettingsUiState.Success(currentSettings)
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            runCatching { repository.getSettings() }
                .onFailure { error ->
                    _uiState.value = SettingsUiState.Error(
                        error.message ?: "Error al cargar configuracion"
                    )
                }
        }
    }

    fun updateSettings(settings: BusinessSettings) {
        viewModelScope.launch {
            val result = runCatching { repository.updateSettings(settings) }
            result.onFailure { error ->
                _uiState.value = SettingsUiState.Error(
                    error.message ?: "Error al actualizar configuracion"
                )
                return@launch
            }

            if (result.getOrDefault(false).not()) {
                _uiState.value = SettingsUiState.Error("No se pudo actualizar la configuracion")
            }
        }
    }

    // Helpers para obtener configuraciones especificas
    fun isDeliveryEnabled(): Boolean {
        return _settings.value?.deliverySettings?.isDeliveryEnabled ?: false
    }

    fun isPickupEnabled(): Boolean {
        return _settings.value?.deliverySettings?.isPickupEnabled ?: false
    }

    fun getDeliveryFee(): Double {
        return _settings.value?.deliverySettings?.deliveryFee ?: 0.0
    }

    fun getMinimumOrderAmount(): Double {
        return _settings.value?.deliverySettings?.minimumOrderAmount ?: 0.0
    }

    fun isCurrentlyOpen(): Boolean {
        return _settings.value?.businessHours?.isCurrentlyOpen() ?: false
    }
}

/**
 * Estados de UI para pantalla de configuracion
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val settings: BusinessSettings) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
