package com.llego.business.settings.data.repository

import com.llego.business.settings.data.model.BusinessHours
import com.llego.business.settings.data.model.BusinessSettings
import com.llego.business.settings.data.model.DaySchedule
import com.llego.business.settings.data.model.DeliverySettings
import com.llego.business.settings.data.model.NotificationSettings
import com.llego.business.settings.data.model.OrderSettings
import com.llego.business.settings.data.model.PaymentMethod
import com.llego.shared.data.auth.AuthManager
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.repositories.PaymentMethodsRepository
import com.llego.shared.data.model.PaymentMethod as CatalogPaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull

enum class SettingsDataSourceMode {
    REAL_BRANCH,
    MOCK
}

/**
 * Repository for business settings.
 *
 * - REAL_BRANCH: reads/writes branch-backed settings from backend.
 * - MOCK: in-memory mock settings.
 * - allowMockFallback: explicit fallback when REAL_BRANCH cannot resolve a branch context.
 */
class SettingsRepository private constructor(
    tokenManager: TokenManager,
    private val sourceMode: SettingsDataSourceMode,
    private val allowMockFallback: Boolean
) {
    private val authManager = AuthManager.getInstance(tokenManager)
    private val paymentMethodsRepository = PaymentMethodsRepository(tokenManager = tokenManager)

    private val _settings = MutableStateFlow<BusinessSettings?>(null)
    val settings: Flow<BusinessSettings> = _settings.asStateFlow().filterNotNull()

    private var paymentMethodsCache: List<CatalogPaymentMethod>? = null

    suspend fun getSettings(): BusinessSettings {
        return when (sourceMode) {
            SettingsDataSourceMode.MOCK -> {
                val mock = getMockSettings()
                _settings.value = mock
                mock
            }

            SettingsDataSourceMode.REAL_BRANCH -> {
                val branch = resolveCurrentBranch(refreshFromBackend = true)
                if (branch == null) {
                    return fallbackSettingsOrThrow("No se pudo resolver la sucursal actual")
                }

                val resolved = branch.toBusinessSettings(previous = _settings.value)
                _settings.value = resolved
                resolved
            }
        }
    }

    suspend fun updateSettings(settings: BusinessSettings): Boolean {
        return when (sourceMode) {
            SettingsDataSourceMode.MOCK -> {
                _settings.value = settings
                true
            }

            SettingsDataSourceMode.REAL_BRANCH -> {
                val branch = resolveCurrentBranch(refreshFromBackend = false)
                    ?: return false

                val selectedPaymentMethodIds = resolvePaymentMethodIds(
                    selectedMethods = settings.acceptedPaymentMethods,
                    existingBranchPaymentMethodIds = branch.paymentMethodIds
                )
                val paymentMethodIdsForUpdate = selectedPaymentMethodIds.takeIf { it.isNotEmpty() }

                val input = UpdateBranchInput(
                    schedule = settings.businessHours.toBranchSchedule(),
                    paymentMethodIds = paymentMethodIdsForUpdate
                )

                when (val result = authManager.updateBranch(branch.id, input)) {
                    is BusinessResult.Success -> {
                        val merged = result.data.toBusinessSettings(previous = settings)
                        _settings.value = merged
                        true
                    }

                    is BusinessResult.Error -> false
                    BusinessResult.Loading -> false
                }
            }
        }
    }

    private suspend fun resolveCurrentBranch(refreshFromBackend: Boolean): Branch? {
        var branch = authManager.getCurrentBranchSync()

        if (branch == null) {
            val currentBusiness = authManager.getCurrentBusinessSync() ?: run {
                when (val businessesResult = authManager.getBusinesses()) {
                    is BusinessResult.Success -> businessesResult.data.firstOrNull()
                    is BusinessResult.Error -> null
                    BusinessResult.Loading -> null
                }
            }

            if (currentBusiness != null) {
                when (val branchesResult = authManager.getBranches(currentBusiness.id)) {
                    is BusinessResult.Success -> {
                        branch = authManager.getCurrentBranchSync() ?: branchesResult.data.firstOrNull()
                        if (branch != null) {
                            authManager.setCurrentBranch(branch)
                        }
                    }

                    is BusinessResult.Error -> Unit
                    BusinessResult.Loading -> Unit
                }
            }
        }

        if (refreshFromBackend && branch != null) {
            when (val branchResult = authManager.getBranch(branch.id)) {
                is BusinessResult.Success -> {
                    branch = branchResult.data
                    authManager.setCurrentBranch(branch)
                }

                is BusinessResult.Error -> Unit
                BusinessResult.Loading -> Unit
            }
        }

        return branch
    }

    private suspend fun resolvePaymentMethodIds(
        selectedMethods: List<PaymentMethod>,
        existingBranchPaymentMethodIds: List<String>
    ): List<String> {
        val catalog = getPaymentMethodsCatalog()
        if (catalog.isEmpty()) {
            return existingBranchPaymentMethodIds
        }

        val selectedSet = selectedMethods.toSet()
        val selectedKnownIds = catalog
            .filter { method ->
                val enumValue = method.method.toSettingsPaymentMethodOrNull()
                enumValue != null && enumValue in selectedSet
            }
            .map { it.id }

        val catalogIds = catalog.map { it.id }.toSet()
        val unknownExistingIds = existingBranchPaymentMethodIds.filter { it !in catalogIds }

        return (selectedKnownIds + unknownExistingIds).distinct()
    }

    private suspend fun Branch.toBusinessSettings(previous: BusinessSettings?): BusinessSettings {
        val acceptedMethods = resolveAcceptedPaymentMethods(paymentMethodIds)
        val previousSettings = previous ?: getMockSettings()

        return BusinessSettings(
            businessHours = schedule.toBusinessHours(previousSettings.businessHours),
            acceptedPaymentMethods = if (acceptedMethods.isNotEmpty()) {
                acceptedMethods
            } else {
                previousSettings.acceptedPaymentMethods
            },
            deliverySettings = previousSettings.deliverySettings,
            orderSettings = previousSettings.orderSettings,
            notifications = previousSettings.notifications
        )
    }

    private suspend fun resolveAcceptedPaymentMethods(paymentMethodIds: List<String>): List<PaymentMethod> {
        val catalog = getPaymentMethodsCatalog()
        if (catalog.isEmpty()) {
            return paymentMethodIds.mapNotNull { it.toSettingsPaymentMethodOrNull() }.distinct()
        }

        return paymentMethodIds.mapNotNull { id ->
            val methodName = catalog.firstOrNull { it.id == id }?.method
            methodName?.toSettingsPaymentMethodOrNull()
        }.distinct()
    }

    private suspend fun getPaymentMethodsCatalog(): List<CatalogPaymentMethod> {
        paymentMethodsCache?.let { return it }
        val loaded = paymentMethodsRepository.getPaymentMethods().getOrElse { emptyList() }
        paymentMethodsCache = loaded
        return loaded
    }

    private fun fallbackSettingsOrThrow(message: String): BusinessSettings {
        _settings.value?.let { return it }
        if (allowMockFallback) {
            val mock = getMockSettings()
            _settings.value = mock
            return mock
        }
        throw IllegalStateException(message)
    }

    private fun String.toSettingsPaymentMethodOrNull(): PaymentMethod? {
        val value = lowercase().trim()
        return when {
            value.contains("cash") || value.contains("efectivo") -> PaymentMethod.CASH
            value.contains("card") || value.contains("tarjeta") -> PaymentMethod.CARD
            value.contains("transfer") || value.contains("transferencia") -> PaymentMethod.TRANSFER
            value.contains("wallet") || value.contains("billetera") || value.contains("digital") -> PaymentMethod.DIGITAL_WALLET
            else -> null
        }
    }

    private fun Map<String, List<String>>.toBusinessHours(previous: BusinessHours): BusinessHours {
        return BusinessHours(
            monday = dayScheduleFor(keys = listOf("monday", "mon"), fallback = previous.monday),
            tuesday = dayScheduleFor(keys = listOf("tuesday", "tue"), fallback = previous.tuesday),
            wednesday = dayScheduleFor(keys = listOf("wednesday", "wed"), fallback = previous.wednesday),
            thursday = dayScheduleFor(keys = listOf("thursday", "thu"), fallback = previous.thursday),
            friday = dayScheduleFor(keys = listOf("friday", "fri"), fallback = previous.friday),
            saturday = dayScheduleFor(keys = listOf("saturday", "sat"), fallback = previous.saturday),
            sunday = dayScheduleFor(keys = listOf("sunday", "sun"), fallback = previous.sunday)
        )
    }

    private fun Map<String, List<String>>.dayScheduleFor(
        keys: List<String>,
        fallback: DaySchedule
    ): DaySchedule {
        val slots = keys.firstNotNullOfOrNull { key ->
            this[key] ?: this[key.lowercase()]
        } ?: return fallback

        val firstSlot = slots.firstOrNull()?.trim().orEmpty()
        if (firstSlot.isBlank()) {
            return fallback.copy(isOpen = false)
        }

        val parts = firstSlot.split("-")
        if (parts.size != 2) {
            return fallback
        }

        return DaySchedule(
            isOpen = true,
            openTime = parts[0].trim(),
            closeTime = parts[1].trim()
        )
    }

    private fun BusinessHours.toBranchSchedule(): Map<String, List<String>> {
        return mapOf(
            "monday" to monday.toBranchSlots(),
            "tuesday" to tuesday.toBranchSlots(),
            "wednesday" to wednesday.toBranchSlots(),
            "thursday" to thursday.toBranchSlots(),
            "friday" to friday.toBranchSlots(),
            "saturday" to saturday.toBranchSlots(),
            "sunday" to sunday.toBranchSlots()
        )
    }

    private fun DaySchedule.toBranchSlots(): List<String> {
        return if (isOpen) {
            listOf("${openTime.trim()}-${closeTime.trim()}")
        } else {
            emptyList()
        }
    }

    private fun getMockSettings(): BusinessSettings {
        return BusinessSettings(
            businessHours = BusinessHours(
                monday = DaySchedule(true, "11:00", "22:00"),
                tuesday = DaySchedule(true, "11:00", "22:00"),
                wednesday = DaySchedule(true, "11:00", "22:00"),
                thursday = DaySchedule(true, "11:00", "23:00"),
                friday = DaySchedule(true, "11:00", "23:30"),
                saturday = DaySchedule(true, "12:00", "23:30"),
                sunday = DaySchedule(true, "12:00", "21:00")
            ),
            acceptedPaymentMethods = listOf(
                PaymentMethod.CASH,
                PaymentMethod.CARD,
                PaymentMethod.TRANSFER,
                PaymentMethod.DIGITAL_WALLET
            ),
            deliverySettings = DeliverySettings(
                isDeliveryEnabled = true,
                isPickupEnabled = true,
                deliveryRadius = 5.0,
                minimumOrderAmount = 10.0,
                deliveryFee = 2.0,
                freeDeliveryThreshold = 30.0,
                estimatedDeliveryTime = 45
            ),
            orderSettings = OrderSettings(
                autoAcceptOrders = false,
                maxOrdersPerHour = 20,
                prepTimeBuffer = 5,
                allowScheduledOrders = true,
                cancelationPolicy = "Los pedidos pueden cancelarse hasta 10 minutos despues de realizados"
            ),
            notifications = NotificationSettings(
                newOrderSound = true,
                orderStatusUpdates = true,
                customerMessages = true,
                dailySummary = true
            )
        )
    }

    companion object {
        private var instance: SettingsRepository? = null

        fun getInstance(
            tokenManager: TokenManager,
            sourceMode: SettingsDataSourceMode = SettingsDataSourceMode.REAL_BRANCH,
            allowMockFallback: Boolean = false
        ): SettingsRepository {
            val current = instance
            if (current != null &&
                current.sourceMode == sourceMode &&
                current.allowMockFallback == allowMockFallback
            ) {
                return current
            }

            return SettingsRepository(
                tokenManager = tokenManager,
                sourceMode = sourceMode,
                allowMockFallback = allowMockFallback
            ).also { instance = it }
        }
    }
}
