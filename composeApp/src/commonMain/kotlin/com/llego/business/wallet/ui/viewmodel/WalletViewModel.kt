package com.llego.business.wallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llego.business.wallet.data.model.*
import com.llego.business.wallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de la Wallet del negocio
 * Sigue el patrón de arquitectura del proyecto con Repository y UiState
 */
class WalletViewModel : ViewModel() {

    private val repository = WalletRepository.getInstance()

    // Estado de UI
    private val _uiState = MutableStateFlow<WalletUiState>(WalletUiState.Loading)
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    // Datos de la wallet desde el repository
    val wallet: StateFlow<BusinessWallet?> = repository.wallet
    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
    val earningsSummary: StateFlow<Map<String, EarningsSummary>> = repository.earningsSummary

    // Moneda seleccionada
    private val _selectedCurrency = MutableStateFlow(WalletCurrency.USD)
    val selectedCurrency: StateFlow<WalletCurrency> = _selectedCurrency.asStateFlow()

    // Estados de sheets/modales
    private val _showWithdrawalSheet = MutableStateFlow(false)
    val showWithdrawalSheet: StateFlow<Boolean> = _showWithdrawalSheet.asStateFlow()

    private val _showTransferSheet = MutableStateFlow(false)
    val showTransferSheet: StateFlow<Boolean> = _showTransferSheet.asStateFlow()

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    private val _showTransferConfirmSheet = MutableStateFlow(false)
    val showTransferConfirmSheet: StateFlow<Boolean> = _showTransferConfirmSheet.asStateFlow()

    private val _showReportsSheet = MutableStateFlow(false)
    val showReportsSheet: StateFlow<Boolean> = _showReportsSheet.asStateFlow()

    // Datos del formulario de retiro
    private val _withdrawalAmount = MutableStateFlow("")
    val withdrawalAmount: StateFlow<String> = _withdrawalAmount.asStateFlow()

    private val _withdrawalMethod = MutableStateFlow(WithdrawalMethod.BANK_TRANSFER)
    val withdrawalMethod: StateFlow<WithdrawalMethod> = _withdrawalMethod.asStateFlow()

    private val _accountDetails = MutableStateFlow("")
    val accountDetails: StateFlow<String> = _accountDetails.asStateFlow()

    // Mensaje de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadWalletData()
    }

    /**
     * Carga los datos de la wallet
     */
    fun loadWalletData() {
        viewModelScope.launch {
            _uiState.value = WalletUiState.Loading
            try {
                repository.refreshWallet()
                repository.wallet.collect { wallet ->
                    if (wallet != null) {
                        _uiState.value = WalletUiState.Success(wallet)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error(e.message ?: "Error al cargar la wallet")
            }
        }
    }

    /**
     * Cambia la moneda seleccionada
     */
    fun selectCurrency(currency: WalletCurrency) {
        _selectedCurrency.value = currency
    }

    /**
     * Obtiene el balance de una moneda específica
     */
    fun getBalance(currency: WalletCurrency): Double {
        return repository.getBalance(currency)
    }

    /**
     * Muestra el sheet de retiro
     */
    fun showWithdrawalSheet() {
        _withdrawalAmount.value = ""
        _accountDetails.value = ""
        _showWithdrawalSheet.value = true
    }

    /**
     * Oculta el sheet de retiro
     */
    fun hideWithdrawalSheet() {
        _showWithdrawalSheet.value = false
    }

    /**
     * Actualiza el monto de retiro
     */
    fun updateWithdrawalAmount(amount: String) {
        _withdrawalAmount.value = amount
    }

    /**
     * Actualiza el método de retiro
     */
    fun updateWithdrawalMethod(method: WithdrawalMethod) {
        _withdrawalMethod.value = method
    }

    /**
     * Actualiza los detalles de la cuenta
     */
    fun updateAccountDetails(details: String) {
        _accountDetails.value = details
    }

    /**
     * Procesa la solicitud de retiro
     */
    fun requestWithdrawal() {
        viewModelScope.launch {
            try {
                val amount = _withdrawalAmount.value.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = WalletUiState.Error("Monto inválido")
                    return@launch
                }

                if (_accountDetails.value.isBlank()) {
                    _uiState.value = WalletUiState.Error("Ingrese los detalles de la cuenta")
                    return@launch
                }

                _uiState.value = WalletUiState.Loading

                val result = repository.requestWithdrawal(
                    currency = _selectedCurrency.value,
                    amount = amount,
                    method = _withdrawalMethod.value,
                    accountDetails = _accountDetails.value
                )

                result.fold(
                    onSuccess = { withdrawal ->
                        _successMessage.value = "Solicitud de retiro enviada exitosamente"
                        hideWithdrawalSheet()

                        // Actualizar UI state con wallet actualizada
                        repository.wallet.value?.let {
                            _uiState.value = WalletUiState.Success(it)
                        }

                        // Limpiar mensaje después de 3 segundos
                        kotlinx.coroutines.delay(3000)
                        _successMessage.value = null
                    },
                    onFailure = { error ->
                        _uiState.value = WalletUiState.Error(error.message ?: "Error al procesar retiro")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = WalletUiState.Error("Error al procesar retiro: ${e.message}")
            }
        }
    }

    /**
     * Muestra el sheet de historial
     */
    fun showHistorySheet() {
        _showHistorySheet.value = true
    }

    /**
     * Oculta el sheet de historial
     */
    fun hideHistorySheet() {
        _showHistorySheet.value = false
    }

    /**
     * Muestra el sheet de transferencia
     */
    fun showTransferSheet() {
        _showTransferSheet.value = true
    }

    /**
     * Oculta el sheet de transferencia
     */
    fun hideTransferSheet() {
        _showTransferSheet.value = false
    }

    /**
     * Muestra el sheet de reportes
     */
    fun showReportsSheet() {
        _showReportsSheet.value = true
    }

    /**
     * Oculta el sheet de reportes
     */
    fun hideReportsSheet() {
        _showReportsSheet.value = false
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        if (_uiState.value is WalletUiState.Error) {
            repository.wallet.value?.let {
                _uiState.value = WalletUiState.Success(it)
            }
        }
    }

    /**
     * Formatea una fecha para mostrar
     */
    fun formatDate(dateString: String): String {
        // Simplificación - en producción usar una librería de fechas
        return try {
            val parts = dateString.split("T")[0].split("-")
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Formatea un monto para mostrar
     */
    fun formatAmount(amount: Double, currency: WalletCurrency): String {
        val wholePart = kotlin.math.abs(amount).toLong()
        val decimalPart = ((kotlin.math.abs(amount) - wholePart) * 100).toLong().toString().padStart(2, '0')
        return "${currency.symbol}$wholePart.$decimalPart"
    }
}

/**
 * Estados de UI para la Wallet
 */
sealed class WalletUiState {
    object Loading : WalletUiState()
    data class Success(val wallet: BusinessWallet) : WalletUiState()
    data class Error(val message: String) : WalletUiState()
}
