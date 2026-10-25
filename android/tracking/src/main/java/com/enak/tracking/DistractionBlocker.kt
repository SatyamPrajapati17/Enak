package com.enak.tracking

import android.content.Context
import android.content.Intent
import java.util.concurrent.ConcurrentHashMap

object DistractionBlocker {
    @Volatile
    var isFocusModeActive: Boolean = true // Default active to protect user immediately

    // Map of packageName -> daily limit in minutes (default 30 min)
    val appDailyLimits = ConcurrentHashMap<String, Int>().apply {
        put("com.instagram.android", 30)
        put("com.google.android.youtube", 45)
        put("com.zhiliaoapp.musically", 20)
        put("com.ss.android.ugc.trill", 20)
        put("com.twitter.android", 25)
        put("com.reddit.frontpage", 20)
        put("com.facebook.katana", 25)
        put("com.snapchat.android", 20)
        put("com.netflix.mediaclient", 60)
        put("tv.twitch.android.app", 30)
    }

    // Temporary snooze: packageName -> expiry timestamp in ms
    private val snoozedApps = ConcurrentHashMap<String, Long>()

    fun setDailyLimit(packageName: String, limitMinutes: Int) {
        appDailyLimits[packageName] = limitMinutes
    }

    val defaultDistractingPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.netflix.mediaclient",
        "com.pinterest",
        "tv.twitch.android.app",
        "com.discord"
    )

    private val customBlockedPackages = ConcurrentHashMap.newKeySet<String>()

    fun setBlockedPackages(packages: Collection<String>) {
        customBlockedPackages.clear()
        customBlockedPackages.addAll(packages)
    }

    fun isDistraction(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        val lower = packageName.lowercase()
        return defaultDistractingPackages.contains(packageName) ||
                customBlockedPackages.contains(packageName) ||
                lower.contains("instagram") ||
                lower.contains("tiktok") ||
                lower.contains("twitter") ||
                lower.contains("reddit") ||
                lower.contains("facebook") ||
                lower.contains("snapchat")
    }

    fun isSnoozed(packageName: String): Boolean {
        val until = snoozedApps[packageName] ?: return false
        return if (System.currentTimeMillis() < until) {
            true
        } else {
            snoozedApps.remove(packageName)
            false
        }
    }

    fun snoozeApp(packageName: String, minutes: Int) {
        val expiry = System.currentTimeMillis() + (minutes * 60_000L)
        snoozedApps[packageName] = expiry
    }

    fun shouldBlock(packageName: String, usedMinutesToday: Int = 0): Boolean {
        if (isSnoozed(packageName)) return false

        // 1. If Focus Mode is active and it's a distracting app -> Block!
        if (isFocusModeActive && isDistraction(packageName)) {
            return true
        }

        // 2. If app exceeded its configured daily limit -> Block!
        val limit = appDailyLimits[packageName]
        if (limit != null && usedMinutesToday >= limit) {
            return true
        }

        return false
    }

    fun launchBlockerIfActive(
        context: Context,
        packageName: String,
        usedMinutesToday: Int = 0
    ): Boolean {
        if (packageName == context.packageName) return false

        if (shouldBlock(packageName, usedMinutesToday)) {
            val intent = Intent(context, BlockerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra("PACKAGE_NAME", packageName)
                putExtra("IS_HARD_BLOCK", true)
                putExtra(
                    "BLOCK_REASON",
                    if (isFocusModeActive) "Focus Shield Active: Distracting app blocked"
                    else "Daily boundary exceeded (${usedMinutesToday}m used)"
                )
            }
            try {
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}
