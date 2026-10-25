package com.enak.tracking

import android.content.Context
import android.content.Intent

object DistractionBlocker {
    @Volatile
    var isFocusModeActive: Boolean = false

    val defaultDistractingPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically", // TikTok
        "com.ss.android.ugc.trill",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.netflix.mediaclient",
        "com.pinterest",
        "tv.twitch.android.app"
    )

    private val customBlockedPackages = mutableSetOf<String>()

    fun setBlockedPackages(packages: Collection<String>) {
        customBlockedPackages.clear()
        customBlockedPackages.addAll(packages)
    }

    fun isDistraction(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        return defaultDistractingPackages.contains(packageName) ||
                customBlockedPackages.contains(packageName) ||
                packageName.contains("instagram", ignoreCase = true) ||
                packageName.contains("tiktok", ignoreCase = true) ||
                packageName.contains("twitter", ignoreCase = true) ||
                packageName.contains("reddit", ignoreCase = true)
    }

    fun shouldBlock(packageName: String): Boolean {
        return isFocusModeActive && isDistraction(packageName)
    }

    fun launchBlockerIfActive(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return false
        if (shouldBlock(packageName)) {
            val intent = Intent(context, BlockerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("PACKAGE_NAME", packageName)
                putExtra("WARNING_NUMBER", 2)
                putExtra("IS_HARD_BLOCK", true)
            }
            context.startActivity(intent)
            return true
        }
        return false
    }
}
