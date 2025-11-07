package com.llego.nichos.common.data.repository

import com.llego.nichos.common.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository para gestión de Wallet
 * Maneja los datos de balance, transacciones y retiros
 */
class WalletRepository private constructor() {

    // Flows de datos
    private val _wallet = MutableStateFlow<BusinessWallet?>(null)
    val wallet: StateFlow<BusinessWallet?> = _wallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _earningsSummary = MutableStateFlow<Map<String, EarningsSummary>>(emptyMap())
    val earningsSummary: StateFlow<Map<String, EarningsSummary>> = _earningsSummary.asStateFlow()

    init {
        // Cargar datos mock iniciales
        loadMockData()
    }

    /**
     * Carga datos mock (en producción esto vendría del backend)
     */
    private fun loadMockData() {
        _wallet.value = BusinessWallet(
            businessId = "business_123",
            balances = mapOf(
                WalletCurrency.USD to 1250.75,
                WalletCurrency.CUP to 45600.00
            ),
            totalEarnings = mapOf(
                WalletCurrency.USD to 12500.00,
                WalletCurrency.CUP to 456000.00
            ),
            pendingWithdrawals = listOf(
                WithdrawalRequest(
                    id = "wd_001",
                    businessId = "business_123",
                    currency = WalletCurrency.USD,
                    amount = 500.0,
                    method = WithdrawalMethod.BANK_TRANSFER,
                    accountDetails = "****1234",
                    status = TransactionStatus.PROCESSING,
                    requestedAt = "2025-10-28T10:00:00Z"
                )
            ),
            isVerified = true,
            lastUpdated = "2025-10-30T12:00:00Z"
        )

        _transactions.value = generateMockTransactions()

        _earningsSummary.value = mapOf(
            "today" to EarningsSummary(
                period = "today",
                currency = WalletCurrency.USD,
                totalIncome = 450.50,
                totalWithdrawals = 0.0,
                totalFees = 22.50,
                netEarnings = 428.00,
                transactionCount = 12
            ),
            "week" to EarningsSummary(
                period = "week",
                currency = WalletCurrency.USD,
                totalIncome = 2850.00,
                totalWithdrawals = 500.0,
                totalFees = 142.50,
                netEarnings = 2207.50,
                transactionCount = 78
            ),
            "month" to EarningsSummary(
                period = "month",
                currency = WalletCurrency.USD,
                totalIncome = 12500.00,
                totalWithdrawals = 1500.0,
                totalFees = 625.00,
                netEarnings = 10375.00,
                transactionCount = 324
            )
        )
    }

    /**
     * Recarga los datos de la wallet
     */
    suspend fun refreshWallet() {
        delay(500) // Simular llamada de red
        loadMockData()
    }

    /**
     * Solicita un retiro de fondos
     */
    suspend fun requestWithdrawal(
        currency: WalletCurrency,
        amount: Double,
        method: WithdrawalMethod,
        accountDetails: String
    ): Result<WithdrawalRequest> {
        return try {
            // Validaciones
            val currentBalance = _wallet.value?.balances?.get(currency) ?: 0.0
            if (amount > currentBalance) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            if (amount <= 0) {
                return Result.failure(Exception("Monto inválido"))
            }

            // Simular llamada de red
            delay(1000)

            // Crear solicitud de retiro
            val withdrawal = WithdrawalRequest(
                id = "wd_${kotlin.random.Random.nextInt(10000, 99999)}",
                businessId = _wallet.value?.businessId ?: "",
                currency = currency,
                amount = amount,
                method = method,
                accountDetails = accountDetails,
                status = TransactionStatus.PENDING,
                requestedAt = getCurrentTimestamp()
            )

            // Actualizar balance
            val currentWallet = _wallet.value
            if (currentWallet != null) {
                val newBalances = currentWallet.balances.toMutableMap()
                newBalances[currency] = currentBalance - amount

                val newPendingWithdrawals = currentWallet.pendingWithdrawals + withdrawal

                _wallet.value = currentWallet.copy(
                    balances = newBalances,
                    pendingWithdrawals = newPendingWithdrawals,
                    lastUpdated = getCurrentTimestamp()
                )

                // Agregar transacción
                val transaction = WalletTransaction(
                    id = "tx_${kotlin.random.Random.nextInt(10000, 99999)}",
                    businessId = currentWallet.businessId,
                    type = TransactionType.WITHDRAWAL,
                    currency = currency,
                    amount = -amount,
                    description = "Retiro solicitado - ${method.displayName}",
                    status = TransactionStatus.PENDING,
                    createdAt = getCurrentTimestamp()
                )

                _transactions.value = listOf(transaction) + _transactions.value
            }

            Result.success(withdrawal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el balance de una moneda específica
     */
    fun getBalance(currency: WalletCurrency): Double {
        return _wallet.value?.balances?.get(currency) ?: 0.0
    }

    /**
     * Genera transacciones mock
     */
    private fun generateMockTransactions(): List<WalletTransaction> {
        return listOf(
            WalletTransaction(
                id = "tx_001",
                businessId = "business_123",
                type = TransactionType.INCOME,
                currency = WalletCurrency.USD,
                amount = 45.50,
                description = "Pago de pedido #12345",
                relatedOrderId = "order_12345",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-30T11:30:00Z",
                completedAt = "2025-10-30T11:30:05Z"
            ),
            WalletTransaction(
                id = "tx_002",
                businessId = "business_123",
                type = TransactionType.FEE,
                currency = WalletCurrency.USD,
                amount = -2.25,
                description = "Comisión Llego (5%)",
                relatedOrderId = "order_12345",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-30T11:30:05Z",
                completedAt = "2025-10-30T11:30:05Z"
            ),
            WalletTransaction(
                id = "tx_003",
                businessId = "business_123",
                type = TransactionType.INCOME,
                currency = WalletCurrency.CUP,
                amount = 1200.00,
                description = "Pago de pedido #12346",
                relatedOrderId = "order_12346",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-30T10:15:00Z",
                completedAt = "2025-10-30T10:15:03Z"
            ),
            WalletTransaction(
                id = "tx_004",
                businessId = "business_123",
                type = TransactionType.WITHDRAWAL,
                currency = WalletCurrency.USD,
                amount = -500.00,
                description = "Retiro a cuenta bancaria",
                status = TransactionStatus.PROCESSING,
                createdAt = "2025-10-28T10:00:00Z"
            ),
            WalletTransaction(
                id = "tx_005",
                businessId = "business_123",
                type = TransactionType.REFUND,
                currency = WalletCurrency.USD,
                amount = -25.00,
                description = "Reembolso a cliente - Pedido cancelado",
                relatedOrderId = "order_12340",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-29T15:00:00Z",
                completedAt = "2025-10-29T15:00:02Z"
            ),
            WalletTransaction(
                id = "tx_006",
                businessId = "business_123",
                type = TransactionType.INCOME,
                currency = WalletCurrency.USD,
                amount = 32.00,
                description = "Pago de pedido #12347",
                relatedOrderId = "order_12347",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-30T09:45:00Z",
                completedAt = "2025-10-30T09:45:02Z"
            ),
            WalletTransaction(
                id = "tx_007",
                businessId = "business_123",
                type = TransactionType.FEE,
                currency = WalletCurrency.USD,
                amount = -1.60,
                description = "Comisión Llego (5%)",
                relatedOrderId = "order_12347",
                status = TransactionStatus.COMPLETED,
                createdAt = "2025-10-30T09:45:02Z",
                completedAt = "2025-10-30T09:45:02Z"
            )
        )
    }

    /**
     * Obtiene timestamp actual en formato ISO
     */
    private fun getCurrentTimestamp(): String {
        // En producción usar una librería de fechas multiplataforma como kotlinx-datetime
        return "2025-10-30T${(10..23).random()}:${(10..59).random()}:00Z"
    }

    companion object {
        private var instance: WalletRepository? = null

        fun getInstance(): WalletRepository {
            return instance ?: WalletRepository().also { instance = it }
        }
    }
}
