package com.llego.business.analytics.util

import com.llego.business.orders.ui.viewmodel.OrdersViewModel

enum class PeriodFilter(val displayName: String) {
    DAY("Dia"),
    WEEK("Semana"),
    MONTH("Mes");

    fun getPreviousPeriod(): PeriodFilter {
        return when (this) {
            DAY -> DAY
            WEEK -> WEEK
            MONTH -> MONTH
        }
    }
}

// Funciones helper para obtener datos (mock por ahora)
internal fun getSalesForPeriod(ordersViewModel: OrdersViewModel, period: PeriodFilter): Double {
    // Mock data - en produccion obtener de ordersViewModel
    return when (period) {
        PeriodFilter.DAY -> 1250.50
        PeriodFilter.WEEK -> 8750.75
        PeriodFilter.MONTH -> 35000.00
    }
}

internal fun getSalesChartData(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter
): List<Pair<String, Double>> {
    // Datos de prueba para el grafico
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "00-06" to 120.0,
            "06-12" to 450.0,
            "12-18" to 380.0,
            "18-24" to 300.0
        )
        PeriodFilter.WEEK -> listOf(
            "Lun" to 1200.0,
            "Mar" to 1450.0,
            "Mie" to 1100.0,
            "Jue" to 1650.0,
            "Vie" to 1800.0,
            "Sab" to 2100.0,
            "Dom" to 1450.0
        )
        PeriodFilter.MONTH -> listOf(
            "Sem 1" to 8500.0,
            "Sem 2" to 9200.0,
            "Sem 3" to 8800.0,
            "Sem 4" to 9500.0
        )
    }
}

internal fun getCompletedOrdersCount(ordersViewModel: OrdersViewModel, period: PeriodFilter): Int {
    // Mock data
    return when (period) {
        PeriodFilter.DAY -> 45
        PeriodFilter.WEEK -> 320
        PeriodFilter.MONTH -> 1280
    }
}

internal fun getRejectedOrdersCount(ordersViewModel: OrdersViewModel, period: PeriodFilter): Int {
    // Mock data
    return when (period) {
        PeriodFilter.DAY -> 3
        PeriodFilter.WEEK -> 18
        PeriodFilter.MONTH -> 72
    }
}

internal fun getAverageRating(ordersViewModel: OrdersViewModel): String {
    // Mock data
    return "4.8"
}

internal fun getTopProducts(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter
): List<Pair<String, Int>> {
    // Datos de prueba mas realistas
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "Pizza Margarita" to 25,
            "Hamburguesa Clasica" to 18,
            "Pasta Carbonara" to 15,
            "Ensalada Cesar" to 12,
            "Sandwich Club" to 10
        )
        PeriodFilter.WEEK -> listOf(
            "Pizza Margarita" to 120,
            "Hamburguesa Clasica" to 95,
            "Pasta Carbonara" to 78,
            "Ensalada Cesar" to 65,
            "Sandwich Club" to 52
        )
        PeriodFilter.MONTH -> listOf(
            "Pizza Margarita" to 485,
            "Hamburguesa Clasica" to 392,
            "Pasta Carbonara" to 315,
            "Ensalada Cesar" to 268,
            "Sandwich Club" to 220
        )
    }
}

internal fun getPeakHours(
    ordersViewModel: OrdersViewModel,
    period: PeriodFilter
): List<Pair<String, Int>> {
    // Datos de prueba mas realistas
    return when (period) {
        PeriodFilter.DAY -> listOf(
            "12:00 - 13:00" to 12,
            "19:00 - 20:00" to 10,
            "13:00 - 14:00" to 8,
            "20:00 - 21:00" to 7,
            "18:00 - 19:00" to 6
        )
        PeriodFilter.WEEK -> listOf(
            "12:00 - 13:00" to 85,
            "19:00 - 20:00" to 72,
            "13:00 - 14:00" to 65,
            "20:00 - 21:00" to 58,
            "18:00 - 19:00" to 52
        )
        PeriodFilter.MONTH -> listOf(
            "12:00 - 13:00" to 342,
            "19:00 - 20:00" to 298,
            "13:00 - 14:00" to 268,
            "20:00 - 21:00" to 245,
            "18:00 - 19:00" to 218
        )
    }
}
