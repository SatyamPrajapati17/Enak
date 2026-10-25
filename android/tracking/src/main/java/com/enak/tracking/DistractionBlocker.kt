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

    // Explicit hard block list (persisted in SharedPreferences)
    // Apps in this set WILL NOT OPEN under any circumstances until unblocked from Enak app!
    private val blockedAppsList = ConcurrentHashMap.newKeySet<String>()

    // Temporary snooze: packageName -> expiry timestamp in ms
    private val snoozedApps = ConcurrentHashMap<String, Long>()

    private const val PREFS_NAME = "enak_blocker_prefs"
    private const val KEY_BLOCKED_PACKAGES = "blocked_packages"
    private const val KEY_FOCUS_ACTIVE = "focus_mode_active"

    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedBlocked = prefs.getStringSet(KEY_BLOCKED_PACKAGES, null)
            if (savedBlocked != null) {
                blockedAppsList.clear()
                blockedAppsList.addAll(savedBlocked)
            }
            if (prefs.contains(KEY_FOCUS_ACTIVE)) {
                isFocusModeActive = prefs.getBoolean(KEY_FOCUS_ACTIVE, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAppBlocked(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        return blockedAppsList.contains(packageName)
    }

    fun setAppBlocked(context: Context?, packageName: String, blocked: Boolean) {
        if (packageName.isEmpty()) return
        if (blocked) {
            blockedAppsList.add(packageName)
            // Immediately cancel any snooze if it's explicitly hard blocked
            snoozedApps.remove(packageName)
        } else {
            blockedAppsList.remove(packageName)
            snoozedApps.remove(packageName)
        }
        if (context != null) {
            persistBlockedApps(context)
        }
    }

    fun setAllBlockedApps(context: Context?, packages: Collection<String>) {
        blockedAppsList.clear()
        blockedAppsList.addAll(packages)
        if (context != null) {
            persistBlockedApps(context)
        }
    }

    fun getBlockedApps(): Set<String> {
        return blockedAppsList.toSet()
    }

    fun setBlockedPackages(packages: Collection<String>) {
        setAllBlockedApps(null, packages)
    }

    fun setFocusModeActive(context: Context?, active: Boolean) {
        isFocusModeActive = active
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_FOCUS_ACTIVE, active).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun persistBlockedApps(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, HashSet(blockedAppsList)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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

    fun isDistraction(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        val lower = packageName.lowercase()
        return defaultDistractingPackages.contains(packageName) ||
                blockedAppsList.contains(packageName) ||
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
        // Only allow snoozing if app is NOT in the hard block list!
        if (isAppBlocked(packageName)) return
        val expiry = System.currentTimeMillis() + (minutes * 60_000L)
        snoozedApps[packageName] = expiry
    }

    fun shouldBlock(packageName: String, usedMinutesToday: Int = 0): Boolean {
        // RULE 1: If the app is in the hard Block List, IT CANNOT BE SNOOZED and MUST BE BLOCKED!
        // It will NOT open under any circumstances until unblocked from Enak app.
        if (isAppBlocked(packageName)) {
            return true
        }

        // For non-hard-blocked apps, check temporary snooze
        if (isSnoozed(packageName)) return false

        // RULE 2: If Focus Mode is active and it's a distracting app -> Block!
        if (isFocusModeActive && isDistraction(packageName)) {
            return true
        }

        // RULE 3: If app exceeded its configured daily limit -> Block!
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
            val isExplicitBlock = isAppBlocked(packageName)
            val intent = Intent(context, BlockerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                putExtra("PACKAGE_NAME", packageName)
                putExtra("IS_HARD_BLOCK", true)
                putExtra("IS_FROM_BLOCK_LIST", isExplicitBlock)
                putExtra(
                    "BLOCK_REASON",
                    if (isExplicitBlock) "App Block List: Locked until unblocked in Enak"
                    else if (isFocusModeActive) "Focus Shield Active: Distracting app blocked"
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
