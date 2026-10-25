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
     * Compute real foreground time for all apps since start of today (00:00).
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

        // queryAndAggregateUsageStats provides exact accumulated foreground milliseconds
        val aggregated = manager.queryAndAggregateUsageStats(startOfDay, now)
        val result = mutableMapOf<String, Long>()

        for ((pkg, stats) in aggregated) {
            val foreground = stats.totalTimeInForeground
            if (foreground > 0L) {
                result[pkg] = foreground
            }
        }
        return result
    }

    /**
     * Detect the currently active foreground package name reliably across Android 8 to 15.
     */
    fun getCurrentForegroundPackage(): String? {
        val manager = usageStatsManager ?: return null
        val now = System.currentTimeMillis()

        // 1. First check usage events over the last 30 seconds
        val events = manager.queryEvents(now - 30_000L, now)
        val event = UsageEvents.Event()
        var currentPkg: String? = null
        var lastTimestamp = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if ((event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                 event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) &&
                event.timeStamp > lastTimestamp
            ) {
                currentPkg = event.packageName
                lastTimestamp = event.timeStamp
            }
        }

        if (!currentPkg.isNullOrEmpty()) {
            return currentPkg
        }

        // 2. Fallback: inspect recent usage stats sorted by lastTimeUsed
        val recentStats = manager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 60_000L,
            now
        )
        return recentStats
            ?.filter { it.lastTimeUsed > 0L && it.packageName != context.packageName }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }
}
