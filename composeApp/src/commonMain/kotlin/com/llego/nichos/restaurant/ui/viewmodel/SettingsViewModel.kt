package com.llego.nichos.restaurant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.nichos.restaurant.data.model.RestaurantSettings
import com.llego.nichos.restaurant.data.model.isCurrentlyOpen
import com.llego.nichos.restaurant.data.repository.RestaurantRepository
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestión de Configuración del Restaurante
 */
class SettingsViewModel(
    tokenManager: TokenManager
) : ViewModel() {

    private val repository = RestaurantRepository.getInstance(tokenManager)

    // Estado de UI
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Settings actuales
    private val _settings = MutableStateFlow<RestaurantSettings?>(null)
    val settings: StateFlow<RestaurantSettings?> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                repository.settings.collect { settings ->
                    _settings.value = settings
                    _uiState.value = SettingsUiState.Success(settings)
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.message ?: "Error al cargar configuración")
            }
        }
    }

    fun updateSettings(settings: RestaurantSettings) {
        viewModelScope.launch {
            try {
                repository.updateSettings(settings)
                _settings.value = settings
            } catch (e: Exception) {
                // TODO: Manejar error
            }
        }
    }

    // Helpers para obtener configuraciones específicas
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
 * Estados de UI para pantalla de configuración
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val settings: RestaurantSettings) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
