package com.enak.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class Purpose(val code: String) {
    LEARN("learn"),
    RELAX("relax"),
    SOCIAL("social"),
    RESEARCH("research"),
    QUICK("quick")
}

enum class Label {
    FOCUSED,
    DRIFTING,
    COMPULSIVE;

    override fun toString(): String = when (this) {
        FOCUSED -> "Focused"
        DRIFTING -> "Drifting"
        COMPULSIVE -> "Compulsive"
    }

    companion object {
        fun fromString(value: String): Label = when (value.lowercase()) {
            "focused" -> FOCUSED
            "drifting" -> DRIFTING
            "compulsive" -> COMPULSIVE
            else -> DRIFTING
        }
    }
}

enum class OverrunState {
    ON_TRACK,
    NEARING,
    OVER,
    FAR_OVER;

    override fun toString(): String = when (this) {
        ON_TRACK -> "on-track"
        NEARING -> "nearing"
        OVER -> "over"
        FAR_OVER -> "far-over"
    }
}

data class Signals(
    val scrollVelocity: Double,
    val flickRate: Double,
    val avgDwell: Double,
    val refreshRate: Double,
    val purposeMatch: Double,
    val hour: Int,
    val baselineRatio: Double
)

data class ScoreResult(
    val score: Int,
    val label: Label,
    val parts: Map<String, Double>
)

object Scoring {
    const val EXIT_LOOP_WINDOW_MS = 120_000L
    const val EXIT_LOOP_THRESHOLD = 3

    private fun clamp(v: Double, lo: Double = 0.0, hi: Double = 1.0): Double =
        max(lo, min(hi, v))

    fun computeScore(s: Signals): ScoreResult {
        val velocity = 1.0 - clamp((s.scrollVelocity - 300.0) / 1500.0)
        val flick = 1.0 - clamp(s.flickRate / 20.0)
        val dwell = clamp((s.avgDwell - 1.0) / 9.0)
        val refresh = 1.0 - clamp(s.refreshRate / 3.0)
        val match = clamp(s.purposeMatch)
        val late = if (s.hour >= 23 || s.hour < 5) 0.6 else 1.0
        val baseline = 1.0 - clamp((s.baselineRatio - 1.5) / 2.5)

        val parts = mapOf(
            "velocity" to velocity,
            "flick" to flick,
            "dwell" to dwell,
            "refresh" to refresh,
            "match" to match,
            "late" to late,
            "baseline" to baseline
        )

        val engagement = dwell * 0.5 + match * 0.5
        val contextWeight = if (engagement > 0.7) 0.02 else 0.1
        val raw = velocity * 0.15 +
                flick * 0.17 +
                dwell * 0.22 +
                refresh * 0.1 +
                match * 0.24 +
                late * (contextWeight * 0.6) +
                baseline * contextWeight

        val maxExpected = 0.88 + contextWeight * 1.6
        val score = (clamp(raw / maxExpected) * 100.0).roundToInt()
        return ScoreResult(score, labelFor(score), parts)
    }

    fun labelFor(score: Int): Label = when {
        score >= 65 -> Label.FOCUSED
        score >= 40 -> Label.DRIFTING
        else -> Label.COMPULSIVE
    }

    fun overrunState(elapsedSec: Double, plannedMin: Double): OverrunState {
        val r = elapsedSec / (plannedMin * 60.0)
        return when {
            r >= 1.5 -> OverrunState.FAR_OVER
            r >= 1.0 -> OverrunState.OVER
            r >= 0.8 -> OverrunState.NEARING
            else -> OverrunState.ON_TRACK
        }
    }

    fun detectExitLoop(returnTimes: List<Long>, now: Long): Boolean {
        return returnTimes.count { now - it <= EXIT_LOOP_WINDOW_MS } >= EXIT_LOOP_THRESHOLD
    }
}
