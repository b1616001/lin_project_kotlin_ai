package com.example.lin_new_project_kotlin_ai

import com.example.lin_new_project_kotlin_ai.`fun`.TimeMethod
import com.example.lin_new_project_kotlin_ai.`fun`.TimeZoneHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeZoneHelperTest {

    @Test
    fun testResolveUtcPlusOffsetDoesNotFallbackToGmtZero() {
        val tz8 = TimeZoneHelper.resolveTimeZone("UTC+8")
        val tzMinus5 = TimeZoneHelper.resolveTimeZone("UTC-5")
        val tzUtc = TimeZoneHelper.resolveTimeZone("UTC")

        assertEquals(8 * 3600 * 1000, tz8.rawOffset)
        assertEquals(-5 * 3600 * 1000, tzMinus5.rawOffset)
        assertEquals(0, tzUtc.rawOffset)
    }

    @Test
    fun testTimeMethodGetTimeZoneProducesDistinctOffsets() {
        val utc = TimeMethod.getTime_zone(TimeMethod.formatType, TimeMethod.zone_UTC)
        val gmt8 = TimeMethod.getTime_zone(TimeMethod.formatType, TimeMethod.zone_GMT_8)
        val gmt9 = TimeMethod.getTime_zone(TimeMethod.formatType, TimeMethod.zone_GMT_9)

        assertNotEquals(utc, gmt8)
        assertNotEquals(gmt8, gmt9)
    }

    @Test
    fun testCrossDayConversionAndIsoFormat() {
        // 2026-11-18 23:30:00 in Asia/Taipei (UTC+8) -> 2026-11-19 00:30:00 in Asia/Tokyo (UTC+9)
        val instant = TimeZoneHelper.buildInstantFromZoneFields(
            year = 2026,
            monthZeroBased = 10, // November
            dayOfMonth = 18,
            hourOfDay = 23,
            minute = 30,
            second = 0,
            sourceZoneId = "Asia/Taipei"
        )

        val tokyo = TimeZoneHelper.convertTimestamp(instant, "Asia/Tokyo", "Asia/Taipei")
        assertEquals("2026-11-19", tokyo.formattedDate)
        assertEquals("00:30:00", tokyo.formattedTime)
        assertEquals(1, tokyo.dayDelta)
        assertEquals("+1 天", tokyo.dayDeltaText)
        assertEquals("2026-11-19T00:30:00+09:00", tokyo.iso8601)

        val utc = TimeZoneHelper.convertTimestamp(instant, "UTC", "Asia/Taipei")
        assertEquals("2026-11-18T15:30:00Z", utc.iso8601)
        assertEquals(0, utc.dayDelta)
    }

    @Test
    fun testOffsetDiffTextAndDstDetection() {
        val winterInstant = TimeZoneHelper.buildInstantFromZoneFields(2026, 0, 15, 12, 0, 0, "UTC")
        val summerInstant = TimeZoneHelper.buildInstantFromZoneFields(2026, 6, 15, 12, 0, 0, "UTC")

        val nyWinter = TimeZoneHelper.convertTimestamp(winterInstant, "America/New_York", "UTC")
        val nySummer = TimeZoneHelper.convertTimestamp(summerInstant, "America/New_York", "UTC")

        assertFalse(nyWinter.isDaylightSaving)
        assertEquals("UTC-05:00", nyWinter.offsetText)

        assertTrue(nySummer.isDaylightSaving)
        assertEquals("UTC-04:00", nySummer.offsetText)

        assertEquals("比來源快 1 小時", TimeZoneHelper.calculateOffsetDiffText("Asia/Taipei", "Asia/Tokyo", winterInstant))
        assertEquals("無時差（同步）", TimeZoneHelper.calculateOffsetDiffText("Asia/Taipei", "Asia/Singapore", winterInstant))
    }

    @Test
    fun testParseFlexibleInput() {
        val secMillis = TimeZoneHelper.parseFlexibleInput("1795000000", "UTC")
        assertEquals(1795000000000L, secMillis)

        val msMillis = TimeZoneHelper.parseFlexibleInput("1795000000123", "UTC")
        assertEquals(1795000000123L, msMillis)

        val strMillis = TimeZoneHelper.parseFlexibleInput("2026-11-18 08:00:00", "Asia/Taipei")
        assertNotNull(strMillis)
        val utcConverted = TimeZoneHelper.convertTimestamp(strMillis!!, "UTC", "Asia/Taipei")
        assertEquals("2026-11-18 00:00:00", utcConverted.formattedDateTime)

        assertNull(TimeZoneHelper.parseFlexibleInput("not-a-date", "UTC"))
    }
}
