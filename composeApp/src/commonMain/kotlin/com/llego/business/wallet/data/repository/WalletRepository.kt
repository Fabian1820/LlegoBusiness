package com.llego.business.wallet.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.exception.ApolloException
import com.llego.business.wallet.data.model.*
import com.llego.multiplatform.graphql.*
import com.llego.multiplatform.graphql.type.DepositInput as GQLDepositInput
import com.llego.multiplatform.graphql.type.TransferInput as GQLTransferInput
import com.llego.multiplatform.graphql.type.WithdrawInput as GQLWithdrawInput
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extension functions para convertir tipos GraphQL a modelos de dominio
 */

// WalletBalanceType a Map<WalletCurrency, Double>
private fun MyWalletQuery.Balance.toBalanceMap(): Map<WalletCurrency, Double> {
    return mapOf(
        WalletCurrency.CUP to local,
        WalletCurrency.USD to usd
    )
}

private fun BranchWalletQuery.Balance.toBalanceMap(): Map<WalletCurrency, Double> {
    return mapOf(
        WalletCurrency.CUP to local,
        WalletCurrency.USD to usd
    )
}

// WalletTransactionType a WalletTransaction
private fun MyWalletTransactionsQuery.MyWalletTransaction.toDomain(): WalletTransaction {
    val transactionType = when (type) {
        "transfer" -> TransactionType.TRANSFER
        "deposit" -> TransactionType.INCOME
        "withdrawal" -> TransactionType.WITHDRAWAL
        else -> TransactionType.ADJUSTMENT
    }

    val transactionStatus = when (status) {
        "pending" -> TransactionStatus.PENDING
        "processing" -> TransactionStatus.PROCESSING
        "completed" -> TransactionStatus.COMPLETED
        "failed" -> TransactionStatus.FAILED
        "cancelled" -> TransactionStatus.CANCELLED
        else -> TransactionStatus.PENDING
    }

    val walletCurrency = when (currency.uppercase()) {
        "USD" -> WalletCurrency.USD
        "LOCAL", "CUP" -> WalletCurrency.CUP
        else -> WalletCurrency.CUP
    }

    return WalletTransaction(
        id = id,
        businessId = toOwnerId ?: fromOwnerId ?: "",
        type = transactionType,
        currency = walletCurrency,
        amount = amount,
        description = description ?: "",
        relatedOrderId = null,
        status = transactionStatus,
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

private fun BranchWalletTransactionsQuery.BranchWalletTransaction.toDomain(): WalletTransaction {
    val transactionType = when (type) {
        "transfer" -> TransactionType.TRANSFER
        "deposit" -> TransactionType.INCOME
        "withdrawal" -> TransactionType.WITHDRAWAL
        else -> TransactionType.ADJUSTMENT
    }

    val transactionStatus = when (status) {
        "pending" -> TransactionStatus.PENDING
        "processing" -> TransactionStatus.PROCESSING
        "completed" -> TransactionStatus.COMPLETED
        "failed" -> TransactionStatus.FAILED
        "cancelled" -> TransactionStatus.CANCELLED
        else -> TransactionStatus.PENDING
    }

    val walletCurrency = when (currency.uppercase()) {
        "USD" -> WalletCurrency.USD
        "LOCAL", "CUP" -> WalletCurrency.CUP
        else -> WalletCurrency.CUP
    }

    return WalletTransaction(
        id = id,
        businessId = toOwnerId ?: fromOwnerId ?: "",
        type = transactionType,
        currency = walletCurrency,
        amount = amount,
        description = description ?: "",
        relatedOrderId = null,
        status = transactionStatus,
        createdAt = createdAt.toString(),
        completedAt = completedAt?.toString()
    )
}

/**
 * Repository para gestión de Wallet con GraphQL
 * Maneja los datos de balance, transacciones y retiros desde el backend
 */
class WalletRepository private constructor() {

    private val apolloClient: ApolloClient = GraphQLClient.apolloClient
    private val tokenManager: TokenManager = TokenManager()

    // Flows de datos
    private val _wallet = MutableStateFlow<BusinessWallet?>(null)
    val wallet: StateFlow<BusinessWallet?> = _wallet.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _earningsSummary = MutableStateFlow<Map<String, EarningsSummary>>(emptyMap())
    val earningsSummary: StateFlow<Map<String, EarningsSummary>> = _earningsSummary.asStateFlow()

    /**
     * Obtiene el balance de la wallet del usuario actual
     */
    suspend fun getMyWallet(): Result<BusinessWallet> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val response = apolloClient.query(MyWalletQuery(token)).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error desconocido"))
            }

            val myWallet = response.data?.myWallet
                ?: return Result.failure(Exception("No se pudo obtener la wallet"))

            val wallet = BusinessWallet(
                businessId = "current", // Se obtiene de la sucursal actual
                balances = myWallet.balance.toBalanceMap(),
                totalEarnings = emptyMap(), // Se calcula desde transacciones
                pendingWithdrawals = emptyList(), // Se obtiene desde transacciones pendientes
                isVerified = myWallet.status == "active",
                lastUpdated = getCurrentTimestamp()
            )

            _wallet.value = wallet
            Result.success(wallet)
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el balance de una sucursal específica
     */
    suspend fun getBranchWallet(branchId: String): Result<BusinessWallet> {
        return try {
            println("WalletRepository.getBranchWallet: branchId=$branchId")
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val response = apolloClient.query(BranchWalletQuery(branchId, token)).execute()

            if (response.hasErrors()) {
                println("WalletRepository.getBranchWallet: errors=${response.errors}")
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error desconocido"))
            }

            val branchWallet = response.data?.branchWallet
                ?: return Result.failure(Exception("No se pudo obtener la wallet de la sucursal"))

            val wallet = BusinessWallet(
                businessId = branchId,
                balances = branchWallet.balance.toBalanceMap(),
                totalEarnings = emptyMap(),
                pendingWithdrawals = emptyList(),
                isVerified = branchWallet.status == "active",
                lastUpdated = getCurrentTimestamp()
            )

            _wallet.value = wallet
            println("WalletRepository.getBranchWallet: ok balances=${wallet.balances}")
            Result.success(wallet)
        } catch (e: ApolloException) {
            println("WalletRepository.getBranchWallet: ApolloException=${e.message}")
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            println("WalletRepository.getBranchWallet: Exception=${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtiene las transacciones del usuario actual
     */
    suspend fun getMyTransactions(
        limit: Int = 50,
        skip: Int = 0,
        currency: String? = null
    ): Result<List<WalletTransaction>> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val response = apolloClient.query(
                MyWalletTransactionsQuery(
                    jwt = token,
                    limit = Optional.present(limit),
                    skip = Optional.present(skip),
                    currency = Optional.presentIfNotNull(currency)
                )
            ).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error desconocido"))
            }

            val transactions = response.data?.myWalletTransactions
                ?.map { it.toDomain() }
                ?: emptyList()

            _transactions.value = transactions
            Result.success(transactions)
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las transacciones de una sucursal
     */
    suspend fun getBranchTransactions(
        branchId: String,
        limit: Int = 50,
        skip: Int = 0,
        currency: String? = null
    ): Result<List<WalletTransaction>> {
        return try {
            println("WalletRepository.getBranchTransactions: branchId=$branchId limit=$limit skip=$skip")
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val response = apolloClient.query(
                BranchWalletTransactionsQuery(
                    branchId = branchId,
                    jwt = token,
                    limit = Optional.present(limit),
                    skip = Optional.present(skip),
                    currency = Optional.presentIfNotNull(currency)
                )
            ).execute()

            if (response.hasErrors()) {
                println("WalletRepository.getBranchTransactions: errors=${response.errors}")
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error desconocido"))
            }

            val transactions = response.data?.branchWalletTransactions
                ?.map { it.toDomain() }
                ?: emptyList()

            _transactions.value = transactions
            println("WalletRepository.getBranchTransactions: ok count=${transactions.size}")
            Result.success(transactions)
        } catch (e: ApolloException) {
            println("WalletRepository.getBranchTransactions: ApolloException=${e.message}")
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            println("WalletRepository.getBranchTransactions: Exception=${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Transfiere dinero desde la wallet del usuario actual
     */
    suspend fun transferMoney(
        toOwnerId: String? = null,
        toOwnerEmail: String? = null,
        toOwnerUsername: String? = null,
        toOwnerType: String,
        amount: Double,
        currency: String,
        description: String? = null
    ): Result<WalletTransaction> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val input = GQLTransferInput(
                toOwnerId = Optional.presentIfNotNull(toOwnerId),
                toOwnerEmail = Optional.presentIfNotNull(toOwnerEmail),
                toOwnerUsername = Optional.presentIfNotNull(toOwnerUsername),
                toOwnerType = toOwnerType,
                amount = amount,
                currency = currency,
                description = Optional.presentIfNotNull(description)
            )

            val response = apolloClient.mutation(TransferMoneyMutation(input, token)).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error en la transferencia"))
            }

            val transaction = response.data?.transferMoney
                ?: return Result.failure(Exception("No se pudo completar la transferencia"))

            // Refrescar wallet y transacciones
            getMyWallet()
            getMyTransactions()

            Result.success(mapTransferToWalletTransaction(transaction))
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deposita dinero en la wallet del usuario
     */
    suspend fun depositMoney(
        amount: Double,
        currency: String,
        source: String,
        description: String? = null
    ): Result<WalletTransaction> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val input = GQLDepositInput(
                amount = amount,
                currency = currency,
                source = source,
                description = Optional.presentIfNotNull(description)
            )

            val response = apolloClient.mutation(DepositMoneyMutation(input, token)).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error en el depósito"))
            }

            val transaction = response.data?.depositMoney
                ?: return Result.failure(Exception("No se pudo completar el depósito"))

            // Refrescar wallet y transacciones
            getMyWallet()
            getMyTransactions()

            Result.success(mapDepositToWalletTransaction(transaction))
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retira dinero de la wallet del usuario
     */
    suspend fun withdrawMoney(
        amount: Double,
        currency: String,
        destination: String,
        description: String? = null
    ): Result<WalletTransaction> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val input = GQLWithdrawInput(
                amount = amount,
                currency = currency,
                destination = destination,
                description = Optional.presentIfNotNull(description)
            )

            val response = apolloClient.mutation(WithdrawMoneyMutation(input, token)).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error en el retiro"))
            }

            val transaction = response.data?.withdrawMoney
                ?: return Result.failure(Exception("No se pudo completar el retiro"))

            // Refrescar wallet y transacciones
            getMyWallet()
            getMyTransactions()

            Result.success(mapWithdrawToWalletTransaction(transaction))
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transfiere dinero desde la wallet de una sucursal
     */
    suspend fun branchTransferMoney(
        branchId: String,
        toOwnerId: String? = null,
        toOwnerEmail: String? = null,
        toOwnerUsername: String? = null,
        toOwnerType: String,
        amount: Double,
        currency: String,
        description: String? = null
    ): Result<WalletTransaction> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val input = GQLTransferInput(
                toOwnerId = Optional.presentIfNotNull(toOwnerId),
                toOwnerEmail = Optional.presentIfNotNull(toOwnerEmail),
                toOwnerUsername = Optional.presentIfNotNull(toOwnerUsername),
                toOwnerType = toOwnerType,
                amount = amount,
                currency = currency,
                description = Optional.presentIfNotNull(description)
            )

            val response = apolloClient.mutation(
                BranchTransferMoneyMutation(branchId, input, token)
            ).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error en la transferencia"))
            }

            val transaction = response.data?.branchTransferMoney
                ?: return Result.failure(Exception("No se pudo completar la transferencia"))

            // Refrescar wallet y transacciones de la sucursal
            getBranchWallet(branchId)
            getBranchTransactions(branchId)

            Result.success(mapBranchTransferToWalletTransaction(transaction))
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retira dinero de la wallet de una sucursal
     */
    suspend fun branchWithdrawMoney(
        branchId: String,
        amount: Double,
        currency: String,
        destination: String,
        description: String? = null
    ): Result<WalletTransaction> {
        return try {
            val token = tokenManager.getToken()
                ?: return Result.failure(Exception("No hay token de autenticación"))

            val input = GQLWithdrawInput(
                amount = amount,
                currency = currency,
                destination = destination,
                description = Optional.presentIfNotNull(description)
            )

            val response = apolloClient.mutation(
                BranchWithdrawMoneyMutation(branchId, input, token)
            ).execute()

            if (response.hasErrors()) {
                return Result.failure(Exception(response.errors?.firstOrNull()?.message ?: "Error en el retiro"))
            }

            val transaction = response.data?.branchWithdrawMoney
                ?: return Result.failure(Exception("No se pudo completar el retiro"))

            // Refrescar wallet y transacciones de la sucursal
            getBranchWallet(branchId)
            getBranchTransactions(branchId)

            Result.success(mapBranchWithdrawToWalletTransaction(transaction))
        } catch (e: ApolloException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recarga los datos de la wallet
     */
    suspend fun refreshWallet(branchId: String? = null) {
        if (branchId != null) {
            getBranchWallet(branchId)
            getBranchTransactions(branchId)
        } else {
            getMyWallet()
            getMyTransactions()
        }
    }

    /**
     * Solicita un retiro de fondos (wrapper para branchWithdrawMoney)
     */
    suspend fun requestWithdrawal(
        branchId: String,
        currency: WalletCurrency,
        amount: Double,
        method: WithdrawalMethod,
        accountDetails: String
    ): Result<WithdrawalRequest> {
        return try {
            if (branchId.isBlank()) {
                return Result.failure(Exception("No hay sucursal activa"))
            }

            // Validaciones
            val currentBalance = _wallet.value?.balances?.get(currency) ?: 0.0
            if (amount > currentBalance) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            if (amount <= 0) {
                return Result.failure(Exception("Monto inválido"))
            }

            val currencyStr = when (currency) {
                WalletCurrency.USD -> "usd"
                WalletCurrency.CUP -> "local"
            }

            // Llamar a branchWithdrawMoney
            val result = branchWithdrawMoney(
                branchId = branchId,
                amount = amount,
                currency = currencyStr,
                destination = accountDetails,
                description = "Retiro ${method.displayName}"
            )

            result.fold(
                onSuccess = { transaction ->
                    // Crear WithdrawalRequest desde la transacción
                    val withdrawal = WithdrawalRequest(
                        id = transaction.id,
                        businessId = transaction.businessId,
                        currency = currency,
                        amount = amount,
                        method = method,
                        accountDetails = accountDetails,
                        status = transaction.status,
                        requestedAt = transaction.createdAt
                    )
                    Result.success(withdrawal)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
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

    // Helper functions para mapear transacciones GraphQL a WalletTransaction

    private fun mapTransferToWalletTransaction(transfer: TransferMoneyMutation.TransferMoney): WalletTransaction {
        val walletCurrency = when (transfer.currency.uppercase()) {
            "USD" -> WalletCurrency.USD
            "LOCAL", "CUP" -> WalletCurrency.CUP
            else -> WalletCurrency.CUP
        }

        val transactionStatus = when (transfer.status) {
            "pending" -> TransactionStatus.PENDING
            "completed" -> TransactionStatus.COMPLETED
            "failed" -> TransactionStatus.FAILED
            else -> TransactionStatus.COMPLETED
        }

        return WalletTransaction(
            id = transfer.id,
            businessId = transfer.fromOwnerId ?: "",
            type = TransactionType.TRANSFER,
            currency = walletCurrency,
            amount = -transfer.amount, // Negativo porque es salida
            description = transfer.description ?: "Transferencia",
            status = transactionStatus,
            createdAt = transfer.createdAt.toString(),
            completedAt = transfer.completedAt?.toString()
        )
    }

    private fun mapDepositToWalletTransaction(deposit: DepositMoneyMutation.DepositMoney): WalletTransaction {
        val walletCurrency = when (deposit.currency.uppercase()) {
            "USD" -> WalletCurrency.USD
            "LOCAL", "CUP" -> WalletCurrency.CUP
            else -> WalletCurrency.CUP
        }

        val transactionStatus = when (deposit.status) {
            "pending" -> TransactionStatus.PENDING
            "completed" -> TransactionStatus.COMPLETED
            "failed" -> TransactionStatus.FAILED
            else -> TransactionStatus.COMPLETED
        }

        return WalletTransaction(
            id = deposit.id,
            businessId = deposit.toOwnerId ?: "",
            type = TransactionType.INCOME,
            currency = walletCurrency,
            amount = deposit.amount,
            description = deposit.description ?: "Depósito",
            status = transactionStatus,
            createdAt = deposit.createdAt.toString(),
            completedAt = deposit.completedAt?.toString()
        )
    }

    private fun mapWithdrawToWalletTransaction(withdraw: WithdrawMoneyMutation.WithdrawMoney): WalletTransaction {
        val walletCurrency = when (withdraw.currency.uppercase()) {
            "USD" -> WalletCurrency.USD
            "LOCAL", "CUP" -> WalletCurrency.CUP
            else -> WalletCurrency.CUP
        }

        val transactionStatus = when (withdraw.status) {
            "pending" -> TransactionStatus.PENDING
            "processing" -> TransactionStatus.PROCESSING
            "completed" -> TransactionStatus.COMPLETED
            "failed" -> TransactionStatus.FAILED
            else -> TransactionStatus.PENDING
        }

        return WalletTransaction(
            id = withdraw.id,
            businessId = withdraw.fromOwnerId ?: "",
            type = TransactionType.WITHDRAWAL,
            currency = walletCurrency,
            amount = -withdraw.amount,
            description = withdraw.description ?: "Retiro",
            status = transactionStatus,
            createdAt = withdraw.createdAt.toString(),
            completedAt = withdraw.completedAt?.toString()
        )
    }

    private fun mapBranchTransferToWalletTransaction(transfer: BranchTransferMoneyMutation.BranchTransferMoney): WalletTransaction {
        val walletCurrency = when (transfer.currency.uppercase()) {
            "USD" -> WalletCurrency.USD
            "LOCAL", "CUP" -> WalletCurrency.CUP
            else -> WalletCurrency.CUP
        }

        val transactionStatus = when (transfer.status) {
            "pending" -> TransactionStatus.PENDING
            "completed" -> TransactionStatus.COMPLETED
            "failed" -> TransactionStatus.FAILED
            else -> TransactionStatus.COMPLETED
        }

        return WalletTransaction(
            id = transfer.id,
            businessId = transfer.fromOwnerId ?: "",
            type = TransactionType.TRANSFER,
            currency = walletCurrency,
            amount = -transfer.amount,
            description = transfer.description ?: "Transferencia",
            status = transactionStatus,
            createdAt = transfer.createdAt.toString(),
            completedAt = transfer.completedAt?.toString()
        )
    }

    private fun mapBranchWithdrawToWalletTransaction(withdraw: BranchWithdrawMoneyMutation.BranchWithdrawMoney): WalletTransaction {
        val walletCurrency = when (withdraw.currency.uppercase()) {
            "USD" -> WalletCurrency.USD
            "LOCAL", "CUP" -> WalletCurrency.CUP
            else -> WalletCurrency.CUP
        }

        val transactionStatus = when (withdraw.status) {
            "pending" -> TransactionStatus.PENDING
            "processing" -> TransactionStatus.PROCESSING
            "completed" -> TransactionStatus.COMPLETED
            "failed" -> TransactionStatus.FAILED
            else -> TransactionStatus.PENDING
        }

        return WalletTransaction(
            id = withdraw.id,
            businessId = withdraw.fromOwnerId ?: "",
            type = TransactionType.WITHDRAWAL,
            currency = walletCurrency,
            amount = -withdraw.amount,
            description = withdraw.description ?: "Retiro",
            status = transactionStatus,
            createdAt = withdraw.createdAt.toString(),
            completedAt = withdraw.completedAt?.toString()
        )
    }

    /**
     * Obtiene timestamp actual en formato ISO
     */
    private fun getCurrentTimestamp(): String {
        return kotlinx.datetime.Clock.System.now().toString()
    }

    companion object {
        private var instance: WalletRepository? = null

        fun getInstance(): WalletRepository {
            return instance ?: WalletRepository().also { instance = it }
        }
    }
}
