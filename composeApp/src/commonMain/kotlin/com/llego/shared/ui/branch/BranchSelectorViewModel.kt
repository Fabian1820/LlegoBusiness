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
    val error: String? = null
)

class BranchSelectorViewModel(
    private val repository: BusinessRepository = BusinessRepository(tokenManager = TokenManager())
) : ViewModel() {

    private val _uiState = MutableStateFlow(BranchSelectorUiState())
    val uiState: StateFlow<BranchSelectorUiState> = _uiState.asStateFlow()

    fun loadBusinesses() {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getBusinessesWithBranches()) {
                is BusinessResult.Success -> {
                    _uiState.value = BranchSelectorUiState(
                        businessesWithBranches = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is BusinessResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}
