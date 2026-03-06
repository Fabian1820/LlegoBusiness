package com.llego.shared.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.shared.data.model.PaymentMethod
import com.llego.shared.data.repositories.PaymentMethodsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentMethodsUiState(
    val isLoading: Boolean = false,
    val methods: List<PaymentMethod> = emptyList(),
    val error: String? = null
)

class PaymentMethodsViewModel(
    private val repository: PaymentMethodsRepository = PaymentMethodsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun loadPaymentMethods() {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getPaymentMethods()
                .onSuccess { methods ->
                    val filteredMethods = methods.filterNot { method ->
                        val normalizedMethod = method.method.lowercase().trim()
                        normalizedMethod.contains("wallet") ||
                            normalizedMethod.contains("billetera") ||
                            normalizedMethod.contains("digital")
                    }
                    _uiState.value = PaymentMethodsUiState(
                        isLoading = false,
                        methods = filteredMethods,
                        error = null
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = PaymentMethodsUiState(
                        isLoading = false,
                        methods = emptyList(),
                        error = throwable.message
                    )
                }
        }
    }
}
