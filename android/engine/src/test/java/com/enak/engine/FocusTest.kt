package com.enak.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FocusTest {
    @Test
    fun focusHelpers_mapsBrowserPermissionValues() {
        assertEquals(PermState.PROMPT, Focus.mapPermission("default"))
        assertEquals(PermState.GRANTED, Focus.mapPermission("granted"))
        assertEquals(PermState.DENIED, Focus.mapPermission("denied"))
        assertEquals(PermState.UNSUPPORTED, Focus.mapPermission(null))
    }

    @Test
    fun focusHelpers_warnsNear80PercentAndAtDailyLimit() {
        assertEquals(LimitStatus.OK, Focus.limitStatus(23, 30))
        assertEquals(LimitStatus.NEAR, Focus.limitStatus(24, 30))
        assertEquals(LimitStatus.OVER, Focus.limitStatus(30, 30))
        assertEquals(LimitStatus.OK, Focus.limitStatus(500, null))
    }

    @Test
    fun focusHelpers_countsConsecutiveSessionDays() {
        // Date: 2026-10-25 12:00:00 (Month 9 in 0-indexed Calendar is October)
        val calNow = Calendar.getInstance().apply {
            set(2026, 9, 25, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = calNow.timeInMillis

        fun day(n: Int): Long {
            val c = Calendar.getInstance().apply {
                set(2026, 9, 25 - n, 10, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return c.timeInMillis
        }

        assertEquals(3, Focus.streak(listOf(day(0), day(1), day(2), day(4)), now))
        assertEquals(2, Focus.streak(listOf(day(1), day(2)), now))
        assertEquals(0, Focus.streak(listOf(day(3)), now))
    }

    @Test
    fun liveTracking_countsWholeLiveMinutes() {
        assertEquals(0, Focus.liveMinutes(0L, 59_999L))
        assertEquals(2, Focus.liveMinutes(0L, 125_000L))
        assertEquals(0, Focus.liveMinutes(10_000L, 0L))
    }

    @Test
    fun liveTracking_quietHoursWrapPastMidnight() {
        assertTrue(Focus.inQuietHours(23, 22, 7))
        assertTrue(Focus.inQuietHours(3, 22, 7))
        assertFalse(Focus.inQuietHours(12, 22, 7))
    }

    @Test
    fun liveTracking_minutesLeftToLimit() {
        assertEquals(10, Focus.minutesToLimit(20, 30))
        assertEquals(0, Focus.minutesToLimit(40, 30))
    }
}
