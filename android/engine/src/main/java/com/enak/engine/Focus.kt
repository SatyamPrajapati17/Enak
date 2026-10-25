package com.enak.engine

import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class PermState {
    GRANTED,
    DENIED,
    PROMPT,
    UNSUPPORTED;

    override fun toString(): String = when (this) {
        GRANTED -> "granted"
        DENIED -> "denied"
        PROMPT -> "prompt"
        UNSUPPORTED -> "unsupported"
    }

    val label: String
        get() = when (this) {
            GRANTED -> "Allowed"
            DENIED -> "Blocked"
            PROMPT -> "Not asked yet"
            UNSUPPORTED -> "Not supported here"
        }
}

enum class LimitStatus {
    OK,
    NEAR,
    OVER;

    override fun toString(): String = when (this) {
        OK -> "ok"
        NEAR -> "near"
        OVER -> "over"
    }
}

object Focus {
    fun mapPermission(raw: String?): PermState = when (raw) {
        "granted" -> PermState.GRANTED
        "denied" -> PermState.DENIED
        "default", "prompt" -> PermState.PROMPT
        else -> PermState.UNSUPPORTED
    }

    /** 'ok' under 80%, 'near' from 80%, 'over' at or past the limit. No limit -> 'ok'. */
    fun limitStatus(usedMin: Int, limitMin: Int?): LimitStatus {
        if (limitMin == null || limitMin <= 0) return LimitStatus.OK
        if (usedMin >= limitMin) return LimitStatus.OVER
        if (usedMin.toDouble() >= limitMin.toDouble() * 0.8) return LimitStatus.NEAR
        return LimitStatus.OK
    }

    fun dayKey(timeMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val year = cal.get(Calendar.YEAR)
        val month = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
        val day = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        return "$year-$month-$day"
    }

    /** Consecutive days (ending today, or yesterday if nothing today yet) with at least one session. */
    fun streak(starts: List<Long>, now: Long): Int {
        val days = starts.map { dayKey(it) }.toSet()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        if (!days.contains(dayKey(cal.timeInMillis))) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var count = 0
        while (days.contains(dayKey(cal.timeInMillis))) {
            count++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return count
    }

    /** Whole minutes elapsed since a live tracking start (never negative). */
    fun liveMinutes(start: Long, now: Long): Int {
        return max(0, ((now - start) / 60_000L).toInt())
    }

    /** True when hour h falls in quiet hours [start, end), wrapping past midnight. */
    fun inQuietHours(h: Int, start: Int, end: Int): Boolean {
        if (start == end) return false
        return if (start < end) {
            h in start until end
        } else {
            h >= start || h < end
        }
    }

    /** Minutes until the daily limit is reached (0 if already over). */
    fun minutesToLimit(usedMin: Int, limitMin: Int): Int {
        return max(0, limitMin - usedMin)
    }
}
