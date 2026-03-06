package com.llego.shared.ui.business.state

import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BranchVehicle
import com.llego.shared.data.model.ImageUploadState
import com.llego.shared.ui.components.molecules.DaySchedule
import com.llego.shared.ui.components.molecules.TimeRange

data class BusinessFormState(
    val id: Int,
    val name: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val avatarState: ImageUploadState = ImageUploadState.Idle,
    val branches: List<BranchFormState> = listOf(defaultBranchFormState(1))
)

data class BranchFormState(
    val id: Int,
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val countryCode: String = "+51",
    val instagram: String = "",
    val facebook: String = "",
    val whatsapp: String = "",
    val transferAccounts: String = "",
    val qrPayments: String = "",
    val transferPhones: String = "",
    val latitude: Double = 23.1136,
    val longitude: Double = -82.3666,
    val schedule: Map<String, DaySchedule> = defaultBranchSchedule(),
    val selectedTipos: Set<BranchTipo> = emptySet(),
    val useAppMessaging: Boolean = true,
    val selectedVehicles: Set<BranchVehicle> = emptySet(),
    val selectedPaymentMethodIds: List<String> = emptyList(),
    val exchangeRate: String = "",
    val avatarState: ImageUploadState = ImageUploadState.Idle,
    val coverState: ImageUploadState = ImageUploadState.Idle
)

fun defaultBusinessFormState(id: Int, firstBranchId: Int): BusinessFormState {
    return BusinessFormState(
        id = id,
        branches = listOf(defaultBranchFormState(firstBranchId))
    )
}

fun defaultBranchFormState(id: Int): BranchFormState {
    return BranchFormState(id = id, schedule = defaultBranchSchedule())
}

fun defaultBranchSchedule(): Map<String, DaySchedule> {
    return mapOf(
        "mon" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "tue" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "wed" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "thu" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "fri" to DaySchedule(true, listOf(TimeRange("09:00", "18:00"))),
        "sat" to DaySchedule(false, emptyList()),
        "sun" to DaySchedule(false, emptyList())
    )
}
