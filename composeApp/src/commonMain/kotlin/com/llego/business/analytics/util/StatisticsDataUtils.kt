package com.llego.business.analytics.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

enum class PeriodFilter(val displayName: String) {
    DAY("Dia"),
    WEEK("Semana"),
    MONTH("Mes");

    fun toDateRange(): Pair<String, String> {
        val now = Clock.System.now()
        val timezone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timezone).date
        val toDate = now.toString()

        val fromInstant = when (this) {
            DAY -> today.atStartOfDayIn(timezone)
            WEEK -> {
                val daysFromMonday = today.dayOfWeek.ordinal // 0=Mon, 6=Sun
                today.minus(DatePeriod(days = daysFromMonday)).atStartOfDayIn(timezone)
            }
            MONTH -> {
                LocalDate(today.year, today.month, 1).atStartOfDayIn(timezone)
            }
        }

        return fromInstant.toString() to toDate
    }
}
