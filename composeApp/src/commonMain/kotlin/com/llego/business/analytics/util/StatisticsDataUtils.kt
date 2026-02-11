package com.llego.business.analytics.util

import com.llego.business.orders.data.repository.DashboardStatsPeriod

enum class PeriodFilter(val displayName: String) {
    DAY("Dia"),
    WEEK("Semana"),
    MONTH("Mes");

    fun toDashboardPeriod(): DashboardStatsPeriod = when (this) {
        DAY -> DashboardStatsPeriod.TODAY
        WEEK -> DashboardStatsPeriod.WEEK
        MONTH -> DashboardStatsPeriod.MONTH
    }
}
