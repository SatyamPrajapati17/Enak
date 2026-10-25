package com.enak.engine

object Warnings {
    /** Two warnings per session: at the plan and one minute later, or on drift. */
    fun warningDue(
        elapsedSec: Int,
        plannedMin: Int,
        count: Int,
        lastAt: Long,
        now: Long,
        drifting: Boolean
    ): Boolean {
        if (count >= 2 || (now - lastAt < 60_000L)) return false
        return (plannedMin < 600 && elapsedSec >= plannedMin * 60) || drifting
    }
}
