package com.enak.engine

import java.util.Calendar
import kotlin.math.max

enum class Mood(val code: String) {
    BORED("bored"),
    STRESSED("stressed"),
    TIRED("tired"),
    CURIOUS("curious"),
    CALM("calm")
}

data class TimelinePoint(val t: Long, val score: Int)
data class InterventionRecord(val step: String, val arm: String, val accepted: Boolean, val at: Long)
data class ExtensionRecord(val reason: String, val at: Long)

data class SessionRecord(
    val id: String,
    val start: Long,
    val end: Long,
    val purpose: Purpose,
    val plannedMin: Int,
    val actualMin: Int,
    val goal: String? = null,
    val mood: Mood,
    val avgScore: Double,
    val timeline: List<TimelinePoint> = emptyList(),
    val interventions: List<InterventionRecord> = emptyList(),
    val extensions: List<ExtensionRecord> = emptyList(),
    val exitLoops: Int = 0,
    val achieved: Boolean? = null,
    val regret: Int? = null, // 0-10
    val journal: String? = null,
    val sentiment: String? = null // positive, neutral, negative
)

data class SessionMetrics(
    val overrunRate: Double,
    val avgRegret: Double,
    val acceptance: Double,
    val achievedRate: Double,
    val avgScore: Double
)

data class Trigger(
    val mood: String,
    val whenPart: String,
    val ratio: Double,
    val lift: Double,
    val count: Int
)

data class SmartDefault(
    val purpose: Purpose,
    val plannedMin: Int
)

object Patterns {
    fun median(xs: List<Double>): Double {
        if (xs.isEmpty()) return 0.0
        val s = xs.sorted()
        val m = s.size / 2
        return if (s.size % 2 == 1) s[m] else (s[m - 1] + s[m]) / 2.0
    }

    fun baselineMinutes(sessions: List<SessionRecord>): Int {
        val last20 = sessions.takeLast(20).map { it.actualMin.toDouble() }
        val med = median(last20)
        return if (med > 0) med.toInt() else 10
    }

    fun isOverrun(s: SessionRecord): Boolean = s.actualMin > s.plannedMin * 1.1

    fun metrics(sessions: List<SessionRecord>): SessionMetrics {
        val n = if (sessions.isNotEmpty()) sessions.size.toDouble() else 1.0
        val allInt = sessions.flatMap { it.interventions }
        val rated = sessions.filter { it.regret != null }

        val overrunRate = sessions.count { isOverrun(it) } / n
        val avgRegret = if (rated.isNotEmpty()) rated.sumOf { it.regret ?: 0 }.toDouble() / rated.size.toDouble() else 0.0
        val acceptance = if (allInt.isNotEmpty()) allInt.count { it.accepted }.toDouble() / allInt.size.toDouble() else 0.0
        val achievedRate = sessions.count { it.achieved == true } / n
        val avgScore = sessions.sumOf { it.avgScore } / n

        return SessionMetrics(overrunRate, avgRegret, acceptance, achievedRate, avgScore)
    }

    /** 7x24 grid of average overrun minutes, Monday first (0=Monday, 6=Sunday). */
    fun riskHeatmap(sessions: List<SessionRecord>): List<List<Double>> {
        val grid = MutableList(7) { MutableList(24) { 0.0 } }
        val cnt = MutableList(7) { MutableList(24) { 0 } }

        for (s in sessions) {
            val cal = Calendar.getInstance().apply { timeInMillis = s.start }
            // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2 ... Saturday=7
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val day = (dow + 5) % 7 // Monday=0 ... Sunday=6
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            grid[day][hour] += max(0, s.actualMin - s.plannedMin).toDouble()
            cnt[day][hour]++
        }

        return grid.mapIndexed { dayIdx, row ->
            row.mapIndexed { hourIdx, sumVal ->
                val count = cnt[dayIdx][hourIdx]
                if (count > 0) sumVal / count.toDouble() else 0.0
            }
        }
    }

    fun riskyHours(sessions: List<SessionRecord>, top: Int = 3): List<Int> {
        val byHour = MutableList(24) { 0 }
        for (s in sessions) {
            val cal = Calendar.getInstance().apply { timeInMillis = s.start }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            byHour[h] += max(0, s.actualMin - s.plannedMin)
        }
        return byHour.mapIndexed { h, v -> Pair(h, v) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(top)
            .map { it.first }
    }

    private fun partOfDay(h: Int): String = when {
        h < 12 -> "morning"
        h < 17 -> "afternoon"
        h < 22 -> "evening"
        else -> "late night"
    }

    fun discoverTriggers(sessions: List<SessionRecord>): List<Trigger> {
        if (sessions.isEmpty()) return emptyList()
        val groups = mutableMapOf<String, MutableList<SessionRecord>>()
        for (s in sessions) {
            val cal = Calendar.getInstance().apply { timeInMillis = s.start }
            val k = "${s.mood.code}|${partOfDay(cal.get(Calendar.HOUR_OF_DAY))}"
            groups.getOrPut(k) { mutableListOf() }.add(s)
        }

        val totalRatio = sessions.sumOf { it.actualMin.toDouble() / it.plannedMin.toDouble() }
        val overall = totalRatio / sessions.size.toDouble()

        return groups.entries
            .filter { it.value.size >= 3 }
            .map { (key, groupList) ->
                val parts = key.split("|")
                val mood = parts.getOrNull(0) ?: ""
                val whenPart = parts.getOrNull(1) ?: ""
                val ratio = groupList.sumOf { it.actualMin.toDouble() / it.plannedMin.toDouble() } / groupList.size.toDouble()
                val lift = if (overall > 0) ratio / overall else 1.0
                Trigger(mood, whenPart, ratio, lift, groupList.size)
            }
            .filter { it.lift > 1.2 }
            .sortedByDescending { it.lift }
            .take(4)
    }

    fun smartDefaults(sessions: List<SessionRecord>, hour: Int): SmartDefault {
        val near = sessions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.start }
            kotlin.math.abs(cal.get(Calendar.HOUR_OF_DAY) - hour) <= 2
        }.takeLast(10)

        if (near.isEmpty()) return SmartDefault(Purpose.QUICK, 10)

        val counts = mutableMapOf<Purpose, Int>()
        near.forEach { counts[it.purpose] = (counts[it.purpose] ?: 0) + 1 }
        val purpose = counts.maxByOrNull { it.value }?.key ?: Purpose.QUICK
        val med = median(near.map { it.plannedMin.toDouble() })
        val planned = if (med > 0) med.toInt() else 10
        return SmartDefault(purpose, planned)
    }

    data class RealityCheckOption(val per: Int, val label: String)

    fun realityCheck(min: Int): String {
        val opts = listOf(
            RealityCheckOption(15, "book chapters"),
            RealityCheckOption(12, "km walked"),
            RealityCheckOption(5, "cups of chai brewed")
        )
        val o = opts[max(0, min) % opts.size]
        val n = max(1, (min.toDouble() / o.per.toDouble()).toInt())
        return "$n ${o.label}"
    }
}
