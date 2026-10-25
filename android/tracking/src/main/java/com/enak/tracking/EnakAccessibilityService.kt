package com.enak.tracking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Optional AccessibilityService for instantaneous foreground app detection.
 * Strictly respects Google Play Policy:
 * - Only checks event.packageName to compare against user-defined limit rules.
 * - NEVER reads window content, keystrokes, personal data, or text.
 */
class EnakAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        if (pkgName == packageName) return // Ignore Enak itself

        DistractionBlocker.launchBlockerIfActive(this, pkgName)
    }


    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        DistractionBlocker.init(this)
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }
}
