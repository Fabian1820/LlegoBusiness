package com.llego.shared.data.mappers

import com.llego.shared.data.model.BranchTipo

internal fun parseSchedule(raw: Any?): Map<String, List<String>> {
    val map = raw as? Map<*, *> ?: return emptyMap()
    return map.mapNotNull { (key, value) ->
        val day = key as? String ?: return@mapNotNull null
        val hours = when (value) {
            is String -> listOf(value)
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
        day to hours
    }.toMap()
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
        else -> null
    }
}
