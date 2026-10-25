package com.enak.tracking

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class EnakNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Can snooze or mute notifications during active mindful focus sessions
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
