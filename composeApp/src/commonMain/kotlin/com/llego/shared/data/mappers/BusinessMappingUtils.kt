package com.llego.shared.data.mappers

import com.llego.shared.data.model.BranchTipo
import com.llego.shared.data.model.BranchVehicle

private val DAY_NAMES = listOf("sun", "mon", "tue", "wed", "thu", "fri", "sat")
private val DAY_INDEX_BY_NAME = DAY_NAMES.withIndex().associate { (index, value) -> value to index }

internal fun parseScheduleFromDays(days: List<Triple<Int, Boolean, List<Pair<String, String>>>>): Map<String, List<String>> {
    return days
        .filter { (_, isOpen, hours) -> isOpen && hours.isNotEmpty() }
        .mapNotNull { (day, _, hours) ->
            val dayName = DAY_NAMES.getOrNull(day) ?: return@mapNotNull null
            dayName to hours.map { (open, close) -> "$open-$close" }
        }
        .toMap()
}

internal fun normalizeBranchScheduleForMutation(rawSchedule: Any?): Any? {
    val scheduleMap = rawSchedule as? Map<*, *> ?: return rawSchedule

    val normalizedKeys = scheduleMap.keys
        .mapNotNull { it as? String }
        .map { it.trim().lowercase() }
        .toSet()

    if ("ranges" in normalizedKeys || "days" in normalizedKeys) {
        return rawSchedule
    }

    val legacySchedule = scheduleMap.entries
        .mapNotNull { entry ->
            val rawKey = entry.key as? String ?: return@mapNotNull null
            val normalizedDay = normalizeScheduleDayKey(rawKey)
            if (normalizedDay !in DAY_INDEX_BY_NAME) {
                return@mapNotNull null
            }

            val ranges = (entry.value as? List<*>)?.mapNotNull { value ->
                (value as? String)?.trim()?.takeIf { it.isNotEmpty() }
            } ?: emptyList()

            normalizedDay to ranges
        }
        .toMap()

    val dayRanges = DAY_NAMES.map { dayName ->
        val backendRanges = legacySchedule[dayName].orEmpty()
            .mapNotNull { slot ->
                val parts = slot.split("-", limit = 2)
                val open = parts.getOrNull(0)?.trim()
                val close = parts.getOrNull(1)?.trim()
                if (open.isNullOrEmpty() || close.isNullOrEmpty()) {
                    null
                } else {
                    mapOf("open" to open, "close" to close)
                }
            }

        val dayIndex = DAY_INDEX_BY_NAME[dayName] ?: 0
        mapOf(
            "fromDay" to dayIndex,
            "toDay" to dayIndex,
            "isOpen" to backendRanges.isNotEmpty(),
            "hours" to backendRanges
        )
    }

    return mapOf("ranges" to dayRanges)
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

private fun normalizeScheduleDayKey(key: String): String {
    return when (key.trim().lowercase()) {
        "mon", "lun", "monday", "lunes" -> "mon"
        "tue", "mar", "tuesday", "martes" -> "tue"
        "wed", "mie", "mié", "wednesday", "miercoles", "miércoles" -> "wed"
        "thu", "jue", "thursday", "jueves" -> "thu"
        "fri", "vie", "friday", "viernes" -> "fri"
        "sat", "sab", "sábado", "sabado", "saturday" -> "sat"
        "sun", "dom", "sunday", "domingo" -> "sun"
        else -> key.trim().lowercase()
    }
}
