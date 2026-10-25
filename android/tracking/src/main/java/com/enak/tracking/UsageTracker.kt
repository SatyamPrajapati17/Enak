package com.enak.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val totalTimeMillis: Long,
    val totalMinutes: Int
)

class UsageTracker(private val context: Context) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    /**
     * Compute foreground time for all apps since start of today using UsageEvents.
     */
    fun queryTodayUsage(): Map<String, Long> {
        val manager = usageStatsManager ?: return emptyMap()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val now = System.currentTimeMillis()

        val events = manager.queryEvents(startOfDay, now)
        val event = UsageEvents.Event()

        val lastResumeTimes = mutableMapOf<String, Long>()
        val totalForegroundTimes = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    lastResumeTimes[pkg] = event.timeStamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val resumedAt = lastResumeTimes.remove(pkg)
                    if (resumedAt != null && event.timeStamp > resumedAt) {
                        val duration = event.timeStamp - resumedAt
                        totalForegroundTimes[pkg] = (totalForegroundTimes[pkg] ?: 0L) + duration
                    }
                }
            }
        }

        // Account for any app currently in foreground
        for ((pkg, resumedAt) in lastResumeTimes) {
            if (now > resumedAt) {
                val duration = now - resumedAt
                totalForegroundTimes[pkg] = (totalForegroundTimes[pkg] ?: 0L) + duration
            }
        }

        return totalForegroundTimes
    }

    /**
     * Detect the currently active foreground package name from recent events.
     */
    fun getCurrentForegroundPackage(): String? {
        val manager = usageStatsManager ?: return null
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - 10_000L, now)
        val event = UsageEvents.Event()
        var currentPkg: String? = null
        var lastTimestamp = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp > lastTimestamp) {
                currentPkg = event.packageName
                lastTimestamp = event.timeStamp
            }
        }
        return currentPkg
    }
}
