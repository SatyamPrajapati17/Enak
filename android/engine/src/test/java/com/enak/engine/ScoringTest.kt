package com.enak.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    private val focused = Signals(
        scrollVelocity = 150.0,
        flickRate = 0.0,
        avgDwell = 12.0,
        refreshRate = 0.0,
        purposeMatch = 1.0,
        hour = 15,
        baselineRatio = 1.0
    )

    private val compulsive = Signals(
        scrollVelocity = 2500.0,
        flickRate = 25.0,
        avgDwell = 0.6,
        refreshRate = 4.0,
        purposeMatch = 0.1,
        hour = 1,
        baselineRatio = 4.0
    )

    @Test
    fun overrunStates_isOnTrackBelow80Percent() {
        assertEquals(OverrunState.ON_TRACK, Scoring.overrunState(7.9 * 60, 10.0))
    }

    @Test
    fun overrunStates_isNearingAt80Percent() {
        assertEquals(OverrunState.NEARING, Scoring.overrunState(8.0 * 60, 10.0))
    }

    @Test
    fun overrunStates_isOverAt100Percent() {
        assertEquals(OverrunState.OVER, Scoring.overrunState(10.0 * 60, 10.0))
    }

    @Test
    fun overrunStates_isFarOverAt150Percent() {
        assertEquals(OverrunState.FAR_OVER, Scoring.overrunState(15.0 * 60, 10.0))
    }

    @Test
    fun exitLoop_triggersOn3ReturnsWithin2Minutes() {
        assertTrue(Scoring.detectExitLoop(listOf(0L, 30_000L, 60_000L), 100_000L))
    }

    @Test
    fun exitLoop_ignoresReturnsOlderThan2Minutes() {
        assertFalse(Scoring.detectExitLoop(listOf(0L, 30_000L, 60_000L), 150_000L))
    }

    @Test
    fun exitLoop_needsAtLeast3Returns() {
        assertFalse(Scoring.detectExitLoop(listOf(90_000L, 95_000L), 100_000L))
    }

    @Test
    fun intentionalityScore_labelsDeepOnPurposeEngagementAsFocused() {
        assertEquals(Label.FOCUSED, Scoring.computeScore(focused).label)
    }

    @Test
    fun intentionalityScore_labelsFastOffPurposeFlickingAsCompulsive() {
        assertEquals(Label.COMPULSIVE, Scoring.computeScore(compulsive).label)
    }

    @Test
    fun intentionalityScore_keepsAVeryLongFocusedSessionFocused() {
        val longFocused = focused.copy(baselineRatio = 6.0, hour = 23)
        assertEquals(Label.FOCUSED, Scoring.computeScore(longFocused).label)
    }
}
