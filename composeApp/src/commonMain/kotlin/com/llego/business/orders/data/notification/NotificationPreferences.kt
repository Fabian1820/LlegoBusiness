package com.llego.business.orders.data.notification

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Flag global compartido: ¿debe sonar/mostrarse la alerta cuando entra un pedido nuevo?
 *
 * SettingsRepository lo sincroniza al cargar/guardar `NotificationSettings.newOrderSound`,
 * y OrdersViewModel lo consulta antes de llamar a NotificationService.
 *
 * No expongo el StateFlow al UI porque el toggle vive en `NotificationSettings` (persistido);
 * este object solo actúa como puente para no acoplar OrdersViewModel con SettingsRepository.
 */
object NotificationPreferences {
    private val _newOrderSoundEnabled = MutableStateFlow(true)
    val newOrderSoundEnabled: Boolean get() = _newOrderSoundEnabled.value

    fun setNewOrderSoundEnabled(enabled: Boolean) {
        _newOrderSoundEnabled.value = enabled
    }
}
