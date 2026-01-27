package com.llego.shared.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CreateBusinessInput
import com.llego.shared.data.model.RegisterBranchInput
import com.llego.shared.data.repositories.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State para RegisterBusinessScreen
 */
data class RegisterBusinessUiState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para la pantalla de registro de negocios
 */
class RegisterBusinessViewModel(
    tokenManager: TokenManager = TokenManager()
) : ViewModel() {

    private val businessRepository = BusinessRepository(tokenManager = tokenManager)

    private val _uiState = MutableStateFlow(RegisterBusinessUiState())
    val uiState: StateFlow<RegisterBusinessUiState> = _uiState.asStateFlow()

    /**
     * Registra un nuevo negocio con sus sucursales iniciales
     */
    fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ) {
        println("RegisterBusinessViewModel.registerBusiness: Iniciando...")
        println("RegisterBusinessViewModel.registerBusiness: business=${business}")
        println("RegisterBusinessViewModel.registerBusiness: branches count=${branches.size}")

        viewModelScope.launch {
            println("RegisterBusinessViewModel.registerBusiness: Actualizando estado a loading")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            println("RegisterBusinessViewModel.registerBusiness: Llamando a businessRepository.registerBusiness...")
            when (val result = businessRepository.registerBusiness(business, branches)) {
                is BusinessResult.Success -> {
                    println("RegisterBusinessViewModel.registerBusiness: Success - businessId=${result.data.id}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true,
                        error = null
                    )
                }
                is BusinessResult.Error -> {
                    println("RegisterBusinessViewModel.registerBusiness: Error - message=${result.message}, code=${result.code}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = false,
                        error = result.message
                    )
                }
                else -> {
                    println("RegisterBusinessViewModel.registerBusiness: Estado desconocido (loading?)")
                    // Loading state - no hacer nada
                }
            }
        }
    }

    /**
     * Registra múltiples negocios con sus sucursales
     */
    fun registerMultipleBusinesses(
        businesses: List<Pair<CreateBusinessInput, List<RegisterBranchInput>>>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = businessRepository.registerMultipleBusinesses(businesses)) {
                is BusinessResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true,
                        error = null
                    )
                }
                is BusinessResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    /**
     * Limpia el estado para permitir otro registro
     */
    fun resetState() {
        _uiState.value = RegisterBusinessUiState()
    }
}
