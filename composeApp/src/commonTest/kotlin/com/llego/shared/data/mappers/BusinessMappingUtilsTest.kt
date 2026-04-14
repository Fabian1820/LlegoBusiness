package com.llego.shared.data.mappers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BusinessMappingUtilsTest {

    @Test
    fun `parseScheduleFromDays maps day zero to sunday`() {
        val result = parseScheduleFromDays(
            days = listOf(
                Triple(
                    0,
                    true,
                    listOf("10:00" to "14:00")
                )
            )
        )

        assertEquals(listOf("10:00-14:00"), result["sun"])
        assertTrue("mon" !in result)
    }

    @Test
    fun `normalizeBranchScheduleForMutation converts legacy map to ranges`() {
        val legacySchedule = mapOf(
            "mon" to listOf("09:00-13:00", "17:00-21:00"),
            "sat" to listOf("10:00-14:00")
        )

        val normalized = normalizeBranchScheduleForMutation(legacySchedule) as Map<*, *>
        val ranges = normalized["ranges"] as List<*>

        val sunday = ranges[0] as Map<*, *>
        val monday = ranges[1] as Map<*, *>
        val saturday = ranges[6] as Map<*, *>

        assertEquals(0, sunday["fromDay"])
        assertEquals(false, sunday["isOpen"])
        assertEquals(emptyList<Any>(), sunday["hours"])

        assertEquals(1, monday["fromDay"])
        assertEquals(1, monday["toDay"])
        assertEquals(true, monday["isOpen"])
        assertEquals(
            listOf(
                mapOf("open" to "09:00", "close" to "13:00"),
                mapOf("open" to "17:00", "close" to "21:00")
            ),
            monday["hours"]
        )

        assertEquals(6, saturday["fromDay"])
        assertEquals(true, saturday["isOpen"])
        assertEquals(
            listOf(mapOf("open" to "10:00", "close" to "14:00")),
            saturday["hours"]
        )
    }

    @Test
    fun `normalizeBranchScheduleForMutation keeps new format unchanged`() {
        val newFormat = mapOf(
            "ranges" to listOf(
                mapOf(
                    "fromDay" to 1,
                    "toDay" to 1,
                    "isOpen" to true,
                    "hours" to listOf(mapOf("open" to "09:00", "close" to "18:00"))
                )
            )
        )

        val normalized = normalizeBranchScheduleForMutation(newFormat)

        assertSame(newFormat, normalized)
    }
}

