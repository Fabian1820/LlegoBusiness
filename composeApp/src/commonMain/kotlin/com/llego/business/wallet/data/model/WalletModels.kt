package com.llego.business.wallet.data.model

import kotlinx.serialization.Serializable

/**
 * Modelo de la Wallet para negocios
 * Representa el balance y transacciones del negocio
 */
@Serializable
data class BusinessWallet(
    val businessId: String,
    val balances: Map<WalletCurrency, Double>, // Balance por moneda
    val totalEarnings: Map<WalletCurrency, Double>, // Ingresos totales históricos
    val pendingWithdrawals: List<WithdrawalRequest> = emptyList(),
    val isVerified: Boolean = true, // Si el negocio está verificado para retiros
    val lastUpdated: String
)

/**
 * Monedas soportadas en la Wallet
 */
@Serializable
enum class WalletCurrency(
    val symbol: String,
    val code: String,
) {
    USD("$", "USD"),
    CUP("", "CUP");

    companion object {
        fun fromCode(code: String): WalletCurrency? {
            return entries.find { it.code == code }
        }
    }
}

/**
 * Transacción en la Wallet
 */
@Serializable
data class WalletTransaction(
    val id: String,
    val businessId: String,
    val type: TransactionType,
    val currency: WalletCurrency,
    val amount: Double,
    val description: String,
    val relatedOrderId: String? = null, // ID de pedido relacionado
    val status: TransactionStatus,
    val createdAt: String,
    val completedAt: String? = null
)

/**
 * Tipos de transacciones
 */
@Serializable
enum class TransactionType(val displayName: String) {
    INCOME("Ingreso por pedido"),
    WITHDRAWAL("Retiro solicitado"),
    TRANSFER("Transferencia enviada"),
    REFUND("Reembolso a cliente"),
    FEE("Comisión Llego"),
    ADJUSTMENT("Ajuste manual");
}

/**
 * Estado de la transacción
 */
@Serializable
enum class TransactionStatus(val displayName: String) {
    PENDING("Pendiente"),
    PROCESSING("Procesando"),
    COMPLETED("Completada"),
    FAILED("Fallida"),
    CANCELLED("Cancelada");
}

/**
 * Solicitud de retiro de fondos
 */
@Serializable
data class WithdrawalRequest(
    val id: String,
    val businessId: String,
    val currency: WalletCurrency,
    val amount: Double,
    val method: WithdrawalMethod,
    val accountDetails: String, // Número de cuenta o info de método
    val status: TransactionStatus,
    val requestedAt: String,
    val processedAt: String? = null,
    val notes: String? = null
)

/**
 * Métodos de retiro disponibles
 */
@Serializable
enum class WithdrawalMethod(val displayName: String) {
    BANK_TRANSFER("Transferencia bancaria"),
    MOBILE_PAYMENT("Pago móvil"),
    CASH_PICKUP("Recogida en efectivo"),
    INTERNAL_TRANSFER("Transferencia interna Llego");
}

/**
 * Resumen de ingresos por período
 */
@Serializable
data class EarningsSummary(
    val period: String, // "today", "week", "month"
    val currency: WalletCurrency,
    val totalIncome: Double,
    val totalWithdrawals: Double,
    val totalFees: Double,
    val netEarnings: Double,
    val transactionCount: Int
)
