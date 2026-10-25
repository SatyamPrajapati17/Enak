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
import kotlinx.coroutines.*

class EnakForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trackingJob: Job? = null
    private lateinit var usageTracker: UsageTracker

    override fun onCreate() {
        super.onCreate()
        DistractionBlocker.init(this)
        usageTracker = UsageTracker(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildServiceNotification()
        startForeground(NOTIFICATION_ID, notification)
        startContinuousTracking()
        return START_STICKY
    }

    private fun startContinuousTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val currentPkg = usageTracker.getCurrentForegroundPackage()
                    if (!currentPkg.isNullOrEmpty() && currentPkg != packageName) {
                        val todayUsageMap = usageTracker.queryTodayUsage()
                        val usedMillis = todayUsageMap[currentPkg] ?: 0L
                        val usedMinutes = (usedMillis / 60_000L).toInt()

                        DistractionBlocker.launchBlockerIfActive(
                            context = applicationContext,
                            packageName = currentPkg,
                            usedMinutesToday = usedMinutes
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000L) // Scan every 1 second
            }
        }
    }

    private fun buildServiceNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enak Mindful Shield Active")
            .setContentText("Protecting your attention and monitoring intentionality boundaries.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Enak Shield Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground protection service keeping your focus boundaries active"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        trackingJob?.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "enak_foreground_shield_channel"

        fun start(context: Context) = startService(context)

        fun startService(context: Context) {
            val intent = Intent(context, EnakForegroundService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
