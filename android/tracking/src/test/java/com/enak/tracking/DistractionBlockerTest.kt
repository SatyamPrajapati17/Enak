package com.enak.tracking

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DistractionBlockerTest {

    @Before
    fun setUp() {
        // Reset DistractionBlocker state before each test
        DistractionBlocker.setAllBlockedApps(null, emptySet())
        DistractionBlocker.isFocusModeActive = true
    }

    @Test
    fun appInBlockList_shouldAlwaysBlock() {
        val testPkg = "com.example.distractinggame"

        // Initially not in block list and not a default distraction
        assertFalse(DistractionBlocker.isAppBlocked(testPkg))
        assertFalse(DistractionBlocker.shouldBlock(testPkg, usedMinutesToday = 5))

        // Block app via setAppBlocked
        DistractionBlocker.setAppBlocked(null, testPkg, true)
        assertTrue(DistractionBlocker.isAppBlocked(testPkg))

        // Must now be blocked
        assertTrue(DistractionBlocker.shouldBlock(testPkg, usedMinutesToday = 0))
    }

    @Test
    fun appInBlockList_cannotBeSnoozed() {
        val testPkg = "com.example.addictiveapp"
        DistractionBlocker.setAppBlocked(null, testPkg, true)

        // Attempt to snooze
        DistractionBlocker.snoozeApp(testPkg, 10)

        // Snooze must NOT bypass a hard-blocked app
        assertFalse(DistractionBlocker.isSnoozed(testPkg))
        assertTrue(DistractionBlocker.shouldBlock(testPkg, usedMinutesToday = 0))
    }

    @Test
    fun appUnblocked_canBeOpened() {
        val testPkg = "com.example.studyapp"

        // Block then unblock
        DistractionBlocker.setAppBlocked(null, testPkg, true)
        assertTrue(DistractionBlocker.shouldBlock(testPkg, usedMinutesToday = 0))

        // Unblock from Enak app
        DistractionBlocker.setAppBlocked(null, testPkg, false)
        assertFalse(DistractionBlocker.isAppBlocked(testPkg))
        assertFalse(DistractionBlocker.shouldBlock(testPkg, usedMinutesToday = 0))
    }

    @Test
    fun batchBlockAndUnblock_updatesAllApps() {
        val apps = listOf("com.app.one", "com.app.two", "com.app.three")
        DistractionBlocker.setAllBlockedApps(null, apps)

        assertEquals(3, DistractionBlocker.getBlockedApps().size)
        assertTrue(DistractionBlocker.shouldBlock("com.app.one", 0))
        assertTrue(DistractionBlocker.shouldBlock("com.app.two", 0))
        assertTrue(DistractionBlocker.shouldBlock("com.app.three", 0))

        // Unblock all
        DistractionBlocker.setAllBlockedApps(null, emptySet())
        assertEquals(0, DistractionBlocker.getBlockedApps().size)
        assertFalse(DistractionBlocker.shouldBlock("com.app.one", 0))
    }

    @Test
    fun defaultDistractions_blockedWhenFocusModeActive() {
        DistractionBlocker.isFocusModeActive = true
        assertTrue(DistractionBlocker.shouldBlock("com.instagram.android", 0))

        // When focus mode is turned off and daily limit not reached, not blocked
        DistractionBlocker.isFocusModeActive = false
        assertFalse(DistractionBlocker.shouldBlock("com.instagram.android", 10))

        // But when daily limit (30m) exceeded, still blocked
        assertTrue(DistractionBlocker.shouldBlock("com.instagram.android", 35))
    }
}
