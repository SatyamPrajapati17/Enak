package com.enak.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EnakApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        try {
            com.enak.tracking.EnakForegroundService.startService(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val checkInsChannel = NotificationChannel(
                CHANNEL_CHECK_INS,
                "Session check-ins",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle prompts and intention alignment reminders during sessions"
            }

            val limitsChannel = NotificationChannel(
                CHANNEL_LIMITS,
                "Limits",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when approaching or reaching app daily limits"
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_DAILY_SUMMARY,
                "Daily summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Evening recap of intentionality and screen time insights"
            }

            notificationManager.createNotificationChannels(
                listOf(checkInsChannel, limitsChannel, summaryChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_CHECK_INS = "enak_check_ins"
        const val CHANNEL_LIMITS = "enak_limits"
        const val CHANNEL_DAILY_SUMMARY = "enak_daily_summary"
    }
}
