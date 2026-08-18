package com.fintrack.app.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DateTimeExtensionsTest {

    @Test
    fun toLocalDateTime_and_toEpochMillis_roundTrip() {
        val original = LocalDateTime.of(2026, 8, 18, 14, 30, 0)
        val epochMillis = original.toEpochMillis()
        val convertedBack = epochMillis.toLocalDateTime()

        assertEquals(original.year, convertedBack.year)
        assertEquals(original.month, convertedBack.month)
        assertEquals(original.dayOfMonth, convertedBack.dayOfMonth)
        assertEquals(original.hour, convertedBack.hour)
        assertEquals(original.minute, convertedBack.minute)
    }

    @Test
    fun toLocalDate_returnsCorrectDate() {
        val localDate = LocalDate.of(2026, 8, 18)
        val epochMillis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val result = epochMillis.toLocalDate()

        assertEquals(2026, result.year)
        assertEquals(8, result.monthValue)
        assertEquals(18, result.dayOfMonth)
    }

    @Test
    fun getMonthBounds_returnsAccurateStartAndEndEpochMillis() {
        val date = LocalDateTime.of(2026, 2, 10, 12, 0)
        val (startMillis, endMillis) = getMonthBounds(now = date)

        val startDt = startMillis.toLocalDateTime()
        val endDt = endMillis.toLocalDateTime()

        assertEquals(2026, startDt.year)
        assertEquals(2, startDt.monthValue)
        assertEquals(1, startDt.dayOfMonth)
        assertEquals(0, startDt.hour)
        assertEquals(0, startDt.minute)

        assertEquals(2026, endDt.year)
        assertEquals(2, endDt.monthValue)
        assertEquals(28, endDt.dayOfMonth) // 2026 is non-leap year (28 days in Feb)
        assertEquals(23, endDt.hour)
        assertEquals(59, endDt.minute)

        assertTrue(endMillis > startMillis)
    }
}
