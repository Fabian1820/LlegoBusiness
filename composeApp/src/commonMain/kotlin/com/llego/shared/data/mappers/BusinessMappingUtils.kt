package com.llego.shared.data.mappers

import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BranchVehicle

private val DAY_NAMES = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")

internal fun parseScheduleFromDays(days: List<Triple<Int, Boolean, List<Pair<String, String>>>>): Map<String, List<String>> {
    return days
        .filter { (_, isOpen, hours) -> isOpen && hours.isNotEmpty() }
        .mapNotNull { (day, _, hours) ->
            val dayName = DAY_NAMES.getOrNull(day) ?: return@mapNotNull null
            dayName to hours.map { (open, close) -> "$open-$close" }
        }
        .toMap()
}

internal fun parseStringMap(raw: Any?): Map<String, String>? {
    val map = raw as? Map<*, *> ?: return null
    val parsed = map.mapNotNull { (key, value) ->
        val mapKey = key as? String ?: return@mapNotNull null
        val mapValue = value as? String ?: value?.toString()
        if (mapValue.isNullOrBlank()) null else mapKey to mapValue
    }.toMap()
    return parsed.takeIf { it.isNotEmpty() }
}

internal fun mapBranchTipo(
    gqlTipo: com.llego.multiplatform.graphql.type.BranchTipo?
): BranchTipo? {
    val name = gqlTipo?.name ?: return null
    return when (name) {
        "RESTAURANTE", "RESTAURANT" -> BranchTipo.RESTAURANTE
        "TIENDA", "STORE" -> BranchTipo.TIENDA
        "DULCERIA", "BAKERY" -> BranchTipo.DULCERIA
        "PERFUMERIA", "PERFUME" -> BranchTipo.PERFUMERIA
        else -> null
    }
}

internal fun mapBranchVehicle(
    gqlVehicle: com.llego.multiplatform.graphql.type.BranchVehicle?
): BranchVehicle? {
    val name = gqlVehicle?.name ?: return null
    return when (name) {
        "MOTO" -> BranchVehicle.MOTO
        "BICICLETA" -> BranchVehicle.BICICLETA
        "CARRO" -> BranchVehicle.CARRO
        "CAMIONETA" -> BranchVehicle.CAMIONETA
        "CAMINANDO" -> BranchVehicle.CAMINANDO
        else -> null
    }
}
