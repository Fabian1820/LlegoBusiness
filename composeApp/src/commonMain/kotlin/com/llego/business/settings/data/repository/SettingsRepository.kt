package com.llego.business.settings.data.repository

import com.llego.business.settings.data.model.*
import com.llego.shared.data.auth.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para configuracion del negocio (MVP con datos mock).
 */
class SettingsRepository(
    tokenManager: TokenManager
) {

    private val _settings = MutableStateFlow(getMockSettings())
    val settings: Flow<BusinessSettings> = _settings.asStateFlow()

    suspend fun getSettings(): BusinessSettings {
        delay(300)
        return _settings.value
    }

    suspend fun updateSettings(settings: BusinessSettings): Boolean {
        delay(500)
        _settings.value = settings
        return true
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

        fun getInstance(tokenManager: TokenManager): SettingsRepository {
            return instance ?: SettingsRepository(tokenManager).also { instance = it }
        }
    }
}
