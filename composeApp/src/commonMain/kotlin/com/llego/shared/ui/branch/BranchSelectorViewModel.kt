package com.llego.shared.ui.branch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.data.repositories.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BranchSelectorUiState(
    val businessesWithBranches: List<BusinessWithBranches> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class BranchSelectorViewModel(
    private val repository: BusinessRepository = BusinessRepository(tokenManager = TokenManager())
) : ViewModel() {

    private val _uiState = MutableStateFlow(BranchSelectorUiState(isLoading = true))
    val uiState: StateFlow<BranchSelectorUiState> = _uiState.asStateFlow()
    private var activeUserId: String? = null
    private var requestVersion: Int = 0

    private fun nextRequestVersion(): Int {
        requestVersion += 1
        return requestVersion
    }

    private fun isStaleRequest(version: Int, userId: String?): Boolean {
        return version != requestVersion || userId != activeUserId
    }

    fun onAuthUserChanged(userId: String?) {
        if (userId == activeUserId) {
            return
        }

        activeUserId = userId
        nextRequestVersion() // Invalida requests en vuelo del usuario previo.

        if (userId == null) {
            _uiState.value = BranchSelectorUiState()
            return
        }

        _uiState.value = BranchSelectorUiState(isLoading = true)
        loadBusinesses(forceLoading = true)
    }

    fun clearState() {
        activeUserId = null
        nextRequestVersion()
        _uiState.value = BranchSelectorUiState()
    }

    fun loadBusinesses(forceLoading: Boolean = false) {
        if (activeUserId == null) {
            return
        }

        if (_uiState.value.isLoading && !forceLoading) {
            return
        }

        val hasExistingData = _uiState.value.businessesWithBranches.isNotEmpty()
        val shouldShowLoading = forceLoading || !hasExistingData
        val requestId = nextRequestVersion()
        val requestUserId = activeUserId

        viewModelScope.launch {
            // En cambio de usuario limpiamos datos previos para evitar mostrar negocios de otra cuenta.
            if (shouldShowLoading) {
                _uiState.value = _uiState.value.copy(
                    businessesWithBranches = if (forceLoading) emptyList() else _uiState.value.businessesWithBranches,
                    isLoading = true,
                    isRefreshing = false,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(error = null)
            }

            when (val result = repository.getBusinessesWithBranches()) {
                is BusinessResult.Success -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = BranchSelectorUiState(
                        businessesWithBranches = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is BusinessResult.Error -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message
                    )
                }
                else -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
                }
            }
        }
    }

    fun refreshBusinesses() {
        if (activeUserId == null) {
            return
        }

        if (_uiState.value.isRefreshing || _uiState.value.isLoading) {
            return
        }

        val requestId = nextRequestVersion()
        val requestUserId = activeUserId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            when (val result = repository.getBusinessesWithBranches()) {
                is BusinessResult.Success -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = _uiState.value.copy(
                        businessesWithBranches = result.data,
                        isRefreshing = false,
                        error = null
                    )
                }
                is BusinessResult.Error -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = result.message
                    )
                }
                else -> {
                    if (isStaleRequest(requestId, requestUserId)) return@launch
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            }
        }
    }
}
