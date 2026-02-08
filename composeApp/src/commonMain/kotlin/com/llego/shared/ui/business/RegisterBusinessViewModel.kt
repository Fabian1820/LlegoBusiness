package com.llego.shared.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.CreateBusinessInput
import com.llego.shared.data.model.RegisterBranchInput
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.repositories.BusinessRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

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
    private val registerTimeoutMs = 20_000L

    private val _uiState = MutableStateFlow(RegisterBusinessUiState())
    val uiState: StateFlow<RegisterBusinessUiState> = _uiState.asStateFlow()

    /**
     * Registra un nuevo negocio con sus sucursales iniciales
     */
    fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ) {

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                when (val result = withTimeout(registerTimeoutMs) {
                    businessRepository.registerBusiness(business, branches)
                }) {
                    is BusinessResult.Success -> {
                        applyBranchTransportConfiguration(
                            businessId = result.data.id,
                            branchInputs = branches
                        )
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
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegistered = false,
                            error = "Estado de registro no esperado. Intenta nuevamente."
                        )
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegistered = false,
                    error = "El registro esta tardando demasiado. Revisa tu conexion e intenta nuevamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegistered = false,
                    error = e.message ?: "Error inesperado durante el registro."
                )
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

            try {
                when (val result = withTimeout(registerTimeoutMs) {
                    businessRepository.registerMultipleBusinesses(businesses)
                }) {
                    is BusinessResult.Success -> {
                        result.data.zip(businesses).forEach { (createdBusiness, originalInput) ->
                            applyBranchTransportConfiguration(
                                businessId = createdBusiness.id,
                                branchInputs = originalInput.second
                            )
                        }
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
                    else -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegistered = false,
                            error = "Estado de registro no esperado. Intenta nuevamente."
                        )
                    }
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegistered = false,
                    error = "El registro esta tardando demasiado. Revisa tu conexion e intenta nuevamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegistered = false,
                    error = e.message ?: "Error inesperado durante el registro."
                )
            }
        }
    }

    /**
     * Limpia el estado para permitir otro registro
     */
    fun resetState() {
        _uiState.value = RegisterBusinessUiState()
    }

    private suspend fun applyBranchTransportConfiguration(
        businessId: String,
        branchInputs: List<RegisterBranchInput>
    ) {
        val branchesWithTransportConfig = branchInputs.filter { input ->
            !input.useAppMessaging || input.vehicles.isNotEmpty()
        }
        if (branchesWithTransportConfig.isEmpty()) return

        val branchesResult = businessRepository.getBranches(businessId)
        val backendBranches = (branchesResult as? BusinessResult.Success)?.data ?: return
        if (backendBranches.isEmpty()) return

        val unmatched = backendBranches.toMutableList()
        branchesWithTransportConfig.forEach { branchInput ->
            val targetBranch = pickBranchByName(branchInput.name, unmatched) ?: return@forEach
            unmatched.remove(targetBranch)

            businessRepository.updateBranch(
                branchId = targetBranch.id,
                input = UpdateBranchInput(
                    useAppMessaging = branchInput.useAppMessaging.takeIf {
                        it != targetBranch.useAppMessaging
                    },
                    vehicles = branchInput.vehicles.takeIf {
                        it.toSet() != targetBranch.vehicles.toSet()
                    }
                )
            )
        }
    }

    private fun pickBranchByName(
        expectedName: String,
        candidates: List<Branch>
    ): Branch? {
        val normalized = expectedName.trim().lowercase()
        return candidates.firstOrNull { it.name.trim().lowercase() == normalized }
            ?: candidates.firstOrNull()
    }
}
