package com.enak.engine

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

enum class Arm(val code: String, val displayName: String) {
    BANNER("banner", "Time banner"),
    QUESTION("question", "Reflection question"),
    PAUSE("pause", "Breathing pause"),
    SUGGESTION("suggestion", "Alternative suggestion");

    companion object {
        fun fromCode(code: String): Arm = entries.firstOrNull { it.code == code } ?: BANNER
    }
}

data class ArmStat(val wins: Int, val trials: Int)

typealias ArmStats = Map<Arm, ArmStat>

object Bandit {
    fun emptyStats(): ArmStats = Arm.entries.associateWith { ArmStat(wins = 0, trials = 0) }

    private fun gaussian(rng: () -> Double): Double {
        val u = max(rng(), 1e-9)
        return sqrt(-2.0 * ln(u)) * cos(2.0 * Math.PI * rng())
    }

    // Marsaglia–Tsang gamma sampler (shape >= 1)
    private fun gamma(k: Double, rng: () -> Double): Double {
        if (k < 1.0) return gamma(k + 1.0, rng) * rng().pow(1.0 / k)
        val d = k - 1.0 / 3.0
        val c = 1.0 / sqrt(9.0 * d)
        while (true) {
            var x: Double
            var v: Double
            do {
                x = gaussian(rng)
                v = 1.0 + c * x
            } while (v <= 0.0)
            v = v * v * v
            if (ln(rng()) < 0.5 * x * x + d - d * v + d * ln(v)) {
                return d * v
            }
        }
    }

    fun betaSample(a: Double, b: Double, rng: () -> Double = { Random.nextDouble() }): Double {
        val x = gamma(a, rng)
        val y = gamma(b, rng)
        return x / (x + y)
    }

    fun pickArm(stats: ArmStats, rng: () -> Double = { Random.nextDouble() }): Arm {
        var best = Arm.BANNER
        var bestV = -1.0
        for (a in Arm.entries) {
            val s = stats[a] ?: ArmStat(0, 0)
            val v = betaSample((s.wins + 1).toDouble(), (s.trials - s.wins + 1).toDouble(), rng)
            if (v > bestV) {
                bestV = v
                best = a
            }
        }
        return best
    }

    fun record(stats: ArmStats, arm: Arm, success: Boolean): ArmStats {
        val s = stats[arm] ?: ArmStat(0, 0)
        val updated = stats.toMutableMap()
        updated[arm] = ArmStat(wins = s.wins + if (success) 1 else 0, trials = s.trials + 1)
        return updated
    }

    fun successRate(stat: ArmStat): Double =
        if (stat.trials > 0) stat.wins.toDouble() / stat.trials.toDouble() else 0.0
}
