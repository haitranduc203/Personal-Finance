package com.fintrack.app.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Utility extensions for clean, consistent date-time conversions across the app.
 */
fun Long.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun LocalDateTime.toEpochMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atZone(zone).toInstant().toEpochMilli()

fun LocalDate.toStartOfDayEpochMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

fun LocalDate.toEndOfDayEpochMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

/**
 * Returns epoch millis bounds (start to end) for the month of given LocalDateTime.
 */
fun getMonthBounds(now: LocalDateTime = LocalDateTime.now(), zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val today = now.toLocalDate()
    val firstDay = today.with(TemporalAdjusters.firstDayOfMonth())
    val lastDay = today.with(TemporalAdjusters.lastDayOfMonth())
    val start = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = lastDay.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
    return start to end
}
