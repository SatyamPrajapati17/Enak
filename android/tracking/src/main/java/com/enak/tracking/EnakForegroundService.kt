package com.enak.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.enak.engine.Focus
import com.enak.engine.LimitStatus
import com.enak.engine.Warnings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EnakForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trackingJob: Job? = null
    private lateinit var usageTracker: UsageTracker

    // Package-level tracking for in-memory limit warnings
    private val warningCounts = mutableMapOf<String, Int>()
    private val lastWarningTimes = mutableMapOf<String, Long>()

    // Mock/configured limits per package in minutes (e.g., Instagram, YouTube)
    var appLimitsMinutes = mutableMapOf(
        "com.instagram.android" to 30,
        "com.google.android.youtube" to 45,
        "com.zhiliaoapp.musically" to 20, // TikTok
        "com.twitter.android" to 25
    )

    override fun onCreate() {
        super.onCreate()
        usageTracker = UsageTracker(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildServiceNotification()
        startForeground(NOTIFICATION_ID, notification)
        startTracking()
        return START_STICKY
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (isActive) {
                checkLimitsAndWarn()
                delay(1500L) // Poll every 1-2s while foreground service is active
            }
        }
    }

    private fun checkLimitsAndWarn() {
        val currentPkg = usageTracker.getCurrentForegroundPackage() ?: return
        if (currentPkg == packageName) return // Ignore Enak itself

        if (DistractionBlocker.launchBlockerIfActive(this, currentPkg)) {
            return
        }

        val limitMin = appLimitsMinutes[currentPkg] ?: return

        val usageMap = usageTracker.queryTodayUsage()
        val usedMillis = usageMap[currentPkg] ?: 0L
        val usedMinutes = (usedMillis / 60_000L).toInt()

        val status = Focus.limitStatus(usedMinutes, limitMin)
        val now = System.currentTimeMillis()
        val count = warningCounts[currentPkg] ?: 0
        val lastAt = lastWarningTimes[currentPkg] ?: 0L

        // Warning rules: max 2 warnings before hard block, >= 60s apart
        if (status == LimitStatus.NEAR && count == 0) {
            warningCounts[currentPkg] = 1
            lastWarningTimes[currentPkg] = now
            triggerBlockerOverlay(currentPkg, warningNumber = 1, isHardBlock = false)
        } else if (status == LimitStatus.OVER) {
            if (count < 2 && (now - lastAt >= 60_000L)) {
                warningCounts[currentPkg] = count + 1
                lastWarningTimes[currentPkg] = now
                triggerBlockerOverlay(currentPkg, warningNumber = count + 1, isHardBlock = false)
            } else if (count >= 2) {
                // Hard block reached
                triggerBlockerOverlay(currentPkg, warningNumber = 2, isHardBlock = true)
            }
        }
    }

    private fun triggerBlockerOverlay(pkg: String, warningNumber: Int, isHardBlock: Boolean) {
        val intent = Intent(this, BlockerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("PACKAGE_NAME", pkg)
            putExtra("WARNING_NUMBER", warningNumber)
            putExtra("IS_HARD_BLOCK", isHardBlock)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Limits & Session Check-ins",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground protection of intentional screen time"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildServiceNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enak Intentional Shield")
            .setContentText("Protecting your screen time against drift")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "enak_limits_channel"

        fun start(context: Context) {
            val intent = Intent(context, EnakForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, EnakForegroundService::class.java))
        }
    }
}
