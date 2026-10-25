package com.enak.engine

import org.junit.Assert.assertTrue
import org.junit.Test

class BanditTest {
    @Test
    fun bandit_prefersAnArmThatKeepsWorking() {
        var s = Bandit.emptyStats()
        for (i in 0 until 40) {
            s = Bandit.record(s, Arm.QUESTION, true)
            s = Bandit.record(s, Arm.BANNER, false)
            s = Bandit.record(s, Arm.PAUSE, false)
            s = Bandit.record(s, Arm.SUGGESTION, false)
        }
        val picks = (0 until 50).map { Bandit.pickArm(s) }
        val questionCount = picks.count { it == Arm.QUESTION }
        assertTrue("Expected questionCount > 45, got $questionCount", questionCount > 45)
    }
}
