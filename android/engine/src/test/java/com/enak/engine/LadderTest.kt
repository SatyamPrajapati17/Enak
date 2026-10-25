package com.enak.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LadderTest {
    private val base = LadderInput(
        overrun = OverrunState.ON_TRACK,
        label = Label.DRIFTING,
        exitLoop = false,
        shown = -1,
        sleepGuard = false,
        gentleMode = false
    )

    @Test
    fun interventionLadder_neverInterruptsAFocusedSessionEvenFarOverTime() {
        val input = base.copy(overrun = OverrunState.FAR_OVER, label = Label.FOCUSED)
        assertNull(Ladder.nextStep(input))
    }

    @Test
    fun interventionLadder_startsWithAGentleNudgeWhenNearingTheLimit() {
        val input = base.copy(overrun = OverrunState.NEARING, label = Label.DRIFTING)
        assertEquals(Step.NUDGE, Ladder.nextStep(input))
    }

    @Test
    fun interventionLadder_usesSoftFrictionWhenOverTimeAndCompulsive() {
        val input = base.copy(overrun = OverrunState.OVER, label = Label.COMPULSIVE)
        assertEquals(Step.FRICTION, Ladder.nextStep(input))
    }

    @Test
    fun interventionLadder_doesNotRepeatAStepAlreadyShown() {
        val input = base.copy(overrun = OverrunState.NEARING, label = Label.DRIFTING, shown = 0)
        assertNull(Ladder.nextStep(input))
    }

    @Test
    fun interventionLadder_capsAtReflectionOnGentleDays() {
        val input = base.copy(overrun = OverrunState.FAR_OVER, label = Label.COMPULSIVE, gentleMode = true)
        assertEquals(Step.REFLECT, Ladder.nextStep(input))
    }
}
