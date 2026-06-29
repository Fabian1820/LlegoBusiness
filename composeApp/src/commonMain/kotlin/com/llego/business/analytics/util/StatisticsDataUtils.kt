@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.llego.business.analytics.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
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
        // kotlin.time.Clock.System es la única variante que resuelve en todos los targets
        // (KMP 0.6.1 desincroniza el companion de kotlinx.datetime.Clock entre Android/iOS);
        // convertimos vía epoch a kotlinx Instant para tener toLocalDateTime().
        val now = Instant.fromEpochMilliseconds(kotlin.time.Clock.System.now().toEpochMilliseconds())
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
